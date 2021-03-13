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
	    
	    String[] colNames = new String[columns];
		Class[] colTypes = new Class[columns];
		
		// Determine the names and types of each column
	    for (int x = 1; x < columns + 1; x++) {
	    	colNames[x - 1] = rs.getMetaData().getColumnName(x);
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
