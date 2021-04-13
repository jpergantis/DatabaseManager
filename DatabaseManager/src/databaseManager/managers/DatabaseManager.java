package databaseManager.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class DatabaseManager {

	private File dbFile;
	private boolean hasFile = false;
	private HashMap<String, Class[]> tableColumnTypes = new HashMap<String, Class[]>();
	
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

		// Execute query to determine number of rows in the table
		Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);

	    // Determine the number of rows in the table
	    int rows = (int) rs.getObject(1);
	    
	    // Execute another query to get the actual contents of the table
	    rs = stmt.executeQuery("SELECT * FROM " + tableName);
	    Object[][] tableContent = new Object[rows][rs.getMetaData().getColumnCount()];
	    int columns = rs.getMetaData().getColumnCount();
	    
	    String[] colNames = getColNames(tableName);
		Class[] colTypes;
		
		if (tableColumnTypes.get(tableName) == null) {
			colTypes = new Class[columns];
			// Determine the types of each column
		    for (int x = 1; x < columns + 1; x++) {
		    	try {
		    		colTypes[x - 1] = rs.getObject(x).getClass();
		    	}
		    	catch (NullPointerException e) { // Unsure why this exception was happening, but setting the type to default to Object seems to solve any issue with the problematic table
		    		colTypes[x - 1] = Object.class;
		    	}
		    }
		    
		    tableColumnTypes.put(tableName, colTypes); // Save the column types for later so they do not need to be generated again - it may be good to store this more permanently
		} else {
			colTypes = tableColumnTypes.get(tableName);
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
	    result.setCellSelectionEnabled(true);
	    result.setShowGrid(true);
	    result.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
		 */
		// Prepare the query
		String parsedTextParam = null;
		try {
			parsedTextParam = ((Integer) Integer.parseInt(textParam)).toString(); // Try the value as an integer
		}
		catch (NumberFormatException e1) {
			try {
				parsedTextParam = ((Double) Double.parseDouble(textParam)).toString(); // If it isn't an integer, try it as a double
			}
			catch (NumberFormatException e2) {
				parsedTextParam =  "%" + textParam + "%"; // Otherwise assume it's a string
			}
		}
		
		
			
		
		// Generate a PreparedStatement using text the user enters into the search field
		String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + colParam + " LIKE ?";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setString(1, parsedTextParam);

		// Execute query
	    ResultSet rs = stmt.executeQuery();
	    
	    // Determine the number of rows in the table
	    int rows = (int) rs.getObject(1);
	    
	    // If the number of rows is 0 (meaning this query returned 0 results) return a 1x1 table stating that no results were found.
	    if (rows == 0) {
	    	Object[][] emptyContent = new Object[1][1];
	    	emptyContent[0][0] = "No results found.";
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
	    
	    
	    // You have to execute the query twice because SQLite doesn't support scrollable cursors :(
	    query = "SELECT * FROM " + tableName + " WHERE " + colParam + " LIKE ?";
	    stmt = con.prepareStatement(query);
		stmt.setString(1, parsedTextParam);
	    rs = stmt.executeQuery();
	    
	    int columns = rs.getMetaData().getColumnCount();
	    Object[][] tableContent = new Object[rows][columns];
	    
	    
	    String[] colNames = getColNames(tableName);
		Class[] colTypes = tableColumnTypes.get(tableName); // There is no need to check for validity here, since if we are searching we have already opened the table at least once in this session
		
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
	    result.setShowGrid(true);
	    result.setCellSelectionEnabled(true);
	    result.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    result.setAutoCreateRowSorter(true);
	    return result;
	}
	
	
	
	/**
	 * Adds a row to the given table 
	 * @param table The table into which to insert the new row
	 * @param values An array of Strings ordered by column (left to right)
	 */
	public void addRowToTable(String table, String[] values) throws SQLException {

		// Generate a string containing the names of the columns
		// These do not need to be passed as ?'s in a PreparedStatement because they are not user generated
		String colNamesString = "";
		String[] columnNames = getColNames(table);
		for (int x = 0; x < columnNames.length; x++) {
			if (!values[x].isEmpty()) {
				colNamesString += columnNames[x] + ","; // Column names separated by commas
			}
		}
		
		if (!colNamesString.isEmpty()) {
			colNamesString = colNamesString.substring(0, colNamesString.length() - 1); // Strip the last comma
		}
		
		String questionMarks = "";
		for (String s : values) {
			if (!s.isEmpty())
				questionMarks += "?,";
		}
		
		if (!questionMarks.isEmpty())
			questionMarks = questionMarks.substring(0, questionMarks.length() - 1); // Strip the last comma
				
		// Generate the SQL statement
		String sql = "INSERT INTO " + table + " (" + colNamesString + ") VALUES (" + questionMarks + ");";
		
		// Establish the database connection and finish preparing the statement
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
		
		PreparedStatement stmt = con.prepareStatement(sql);
		int valueAt = 1; // Which ? in the SQL string we are currently at
		for (int x = 0; x < values.length; x++) {
			if (!values[x].isEmpty()) {
				stmt.setString(valueAt, values[x]); // Insert the value into the statement only if it is not empty
				valueAt++;
			}
		}
		
		stmt.executeUpdate();
		
	}
	
	public String[] getColNames(String tableName) throws SQLException {
		
		String[] result;
		ArrayList<String> colNames = new ArrayList<String>(); // We use an ArrayList to temporarily store the column names because it supports dynamic sizing and prevents us from having to query and iterate twice
		
		// Establish database connection
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

		// Execute query		
		Statement stmt = con.createStatement();		
		ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName +");\r\n"); // Each row will contain, among other things, the name of a column in the table
		
		// Add the names of each column to the ArrayList
	    while (rs.next())
	    	colNames.add(rs.getString(2));

	    result = new String[colNames.size()];
		result = colNames.toArray(result);
		return result;
	}
	
	public ArrayList<Boolean> getRequiredColumns(String table) throws SQLException {
		
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		
		// Establish database connection
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

		// Execute query		
		Statement stmt = con.createStatement();		
		ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table +");\r\n"); // Each row will contain, among other things, whether a column is required
			
		// Index 4 in the results of this query indicate whether a field is required
		while (rs.next()) {
			String s = rs.getString(4);
			if (s.toString().equals("1")) {
				result.add(true);
			}
			else {
				result.add(false);
			}
		}
		
		return result;
	}
	
	/**
	 * @param table The table to be searched
	 * @return The Class type of each column in the given table, sorted from left to right
	 * @throws SQLException
	 */
	public Class[] getColumnTypes(String table) {
		return tableColumnTypes.get(table);
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
