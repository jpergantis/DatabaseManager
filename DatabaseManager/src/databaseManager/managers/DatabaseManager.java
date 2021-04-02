package databaseManager.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class DatabaseManager {

	private File dbFile;
	private boolean hasFile = false;
	
	public DatabaseManager() {
		dbFile = null;
	}
	
	public DatabaseManager(File f) {
		dbFile = f;
		// A more rigorous check should be used to determine if the file is a valid database file
		if (dbFile != null) {
			hasFile = true;
		}
	}
	
	/**
	 * Retrieves a list of all tables in the assigned database
	 * @return An ArrayList<String> containing the names of every table in the current working database
	 * @throws SQLException
	 */
	public ArrayList<String> getTables() throws SQLException {
		ArrayList<String> results = new ArrayList<String>();
		
		// Establish db connection
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

		// Retrieve the list of tables
		String[] types = {"TABLE"};
		ResultSet rs = con.getMetaData().getTables(null, null, "%", types);
		
		// Populate the ArrayList with the names of all of the tables in the database
		while(rs.next()) {
			results.add(rs.getString("TABLE_NAME"));
		}
		
		return results;
	}
	
	
	public JTable viewTable(String tableName) throws SQLException {
		
		JTable result;
		
		// Establish database connection
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

		// Execute query
		Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);

	    // Determine the number of rows in the table
	    int rows = 0;
	    while (rs.next())
	    	rows++;
	    
	    // You have to execute the query twice because SQLite doesn't support scrollable cursors :(
	    rs = stmt.executeQuery("SELECT * FROM " + tableName);
	    Object[][] tableContent = new Object[rows][rs.getMetaData().getColumnCount()];
	    int columns = rs.getMetaData().getColumnCount();
	    
	    String[] colNames = getColNames(tableName);
		Class[] colTypes = new Class[columns];
		
		// Determine the types of each column
	    for (int x = 1; x < columns + 1; x++) {
	    	try {
	    		colTypes[x - 1] = rs.getObject(x).getClass();
	    	}
	    	catch (NullPointerException e) { // Unsure why this exception was happening, but setting the type to default to Object seems to solve any issue with the problematic table
	    		colTypes[x - 1] = Object.class;
	    	}
	    }
	    			
		// Fill the JTable 
		while (rs.next()) {
			for (int x = 0; x < columns; x++) {
				tableContent[rs.getRow() - 1][x] = rs.getObject(x + 1);
			}
			
		}
			
		// Create a TableModel containing the results that prevents cell editing, and uses the previously determined class types to sort columns correctly
		@SuppressWarnings("serial")
		TableModel model = new DefaultTableModel(tableContent, colNames) {			
			@Override
			public Class getColumnClass(int columnIndex) {
				return colTypes[columnIndex];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		// Fill and return the JTable
	    result = new JTable(model);
	    result.setAutoCreateRowSorter(true);
	    return result;
	    
	}
	
	public JTable viewTable(String tableName, String colParam, String textParam) throws SQLException {
		JTable result;
		
		// Establish database connection
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

		
		/**
		 * TODO 
		 * This is not really a great way to prepare the query, it should parse based on the type of column of colParam
		 * In fact it would probably be most appropriate to create a separate method or even a class for preparing queries, especially to do so more safely
		 */
		// Prepare the query
		boolean parsed = false;
		String parsedTextParam = null;
		if (!parsed) {
			try {
				parsedTextParam = ((Integer) Integer.parseInt(textParam)).toString();
				parsed = true;
			}
			catch (NumberFormatException e) {
				// Occurs if the given input is not an integer.
				// Don't need to do anything, just try the next format.
			}
		}
		if (!parsed) {
			try {
				parsedTextParam = ((Double) Double.parseDouble(textParam)).toString();
				parsed = true;
			}
			catch (NumberFormatException e) {
				// Occurs if the given input is not a double/float type number.
				// Don't need to do anything, just use it as a string.
			}
		}
		if (!parsed) 
			parsedTextParam =  "'%" + textParam + "%'";
		
		
		String query = "SELECT * FROM " + tableName + " WHERE " + colParam + " LIKE " + parsedTextParam;
		System.out.println(query);
		
		// Execute query
		Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery(query);
	    
	    // If the ResultSet is closed ie there is nothing in it (since we just made the query), return a 1x1 table containing this information
	    if (rs.isClosed()) {
	    	Object[][] emptyContent = new Object[1][1];
	    	emptyContent[0][0] = new String("No results found.");
	    	String[] emptyColNames = {""};
	    	
	    	@SuppressWarnings("serial")
			TableModel model = new DefaultTableModel(emptyContent, emptyColNames) {			
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			
			result = new JTable(model);
			return result;
			
	    }
	    
	    // Determine the number of rows in the table
	    int rows = 0;
	    while (rs.next())
	    	rows++;
	    
	    // You have to execute the query twice because SQLite doesn't support scrollable cursors :(
	    rs = stmt.executeQuery(query);
	    Object[][] tableContent = new Object[rows][rs.getMetaData().getColumnCount()];
	    int columns = rs.getMetaData().getColumnCount();
	    
	    String[] colNames = getColNames(tableName);
		Class[] colTypes = new Class[columns];
		
		// Determine the types of each column
	    for (int x = 1; x < columns + 1; x++) {
	    	try {
	    		colTypes[x - 1] = rs.getObject(x).getClass();
	    	}
	    	catch (NullPointerException e) { // Unsure why this exception was happening, but setting the type to default to Object seems to solve any issue with the problematic table
	    		colTypes[x -1] = Object.class;
	    	}
	    }

		// Fill the JTable 
	 	while (rs.next()) {
	 		for (int x = 0; x < columns; x++) {
	 			tableContent[rs.getRow() - 1][x] = rs.getObject(x + 1);
	 		}				
 		}
			
		// Create a TableModel containing the results that prevents cell editing, and uses the previously determined class types to sort columns correctly
		@SuppressWarnings("serial")
		TableModel model = new DefaultTableModel(tableContent, colNames) {			
			@Override
			public Class getColumnClass(int columnIndex) {
				return colTypes[columnIndex];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		// Fill and return the JTable
	    result = new JTable(model);
	    result.setAutoCreateRowSorter(true);
	    return result;
	}
	
	public String[] getColNames(String tableName) throws SQLException {
		String[] result;
		
		// Establish database connection
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

		// Execute query		
		Statement stmt = con.createStatement();		
		ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName +");\r\n"); // Each row will contain, among other things, the name of a column in the table
		
		// Determine the number of rows in the ResultSet
	    int rows = 0;
	    while (rs.next())
	    	rows++;
		
	    result = new String[rows];
	    
	    // Execute the query again because SQLite doesn't support scrollable cursors
	    rs = stmt.executeQuery("PRAGMA table_info(" + tableName +");\r\n");
	    rs.next();
	    
		for (int x = 0; x < rows; x++) {
			result[x] = rs.getString(2);
			rs.next();
		}
		
		
		return result;
	}
	
	public void setDbFile(File f) {
		dbFile = f;
		if (dbFile != null) {
			hasFile = true;
		}
	}
	
	public boolean hasFile() {
		return hasFile;
	}
	
	public String getFileName() {
		return dbFile.getName();
	}
	
}
