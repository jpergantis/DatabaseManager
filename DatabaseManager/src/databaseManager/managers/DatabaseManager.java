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
		
		// Establish db connection
		Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

		// Execute query
		Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
	    
	    JTable result;
	
	    // Establish number of rows in the table
	    int rows = 0;
	    while (rs.next())
	    	rows++;
	    
	    // You have to execute the query twice because SQLite doesn't support scrollable cursors :(
	    rs = stmt.executeQuery("SELECT * FROM " + tableName);
	    Object[][] tableContent = new Object[rows][rs.getMetaData().getColumnCount()];
	    String[] colNames = new String[rs.getMetaData().getColumnCount()];
	    for (int x = 1; x < colNames.length + 1; x++)
	    	colNames[x - 1] = rs.getMetaData().getColumnName(x);
	    
		// Add the contents of the ResultSet to the table 
		int columns = rs.getMetaData().getColumnCount();
		while (rs.next()) {
			for (int x = 0; x < columns; x++) {
				tableContent[rs.getRow() - 1][x] = rs.getObject(x + 1);
			}
		}
		
		// Fill and return the JTable
		// Use a TableModel that prevents cell editing
		@SuppressWarnings("serial")
		TableModel model = new DefaultTableModel(tableContent, colNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
	    result = new JTable(model);	    
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
