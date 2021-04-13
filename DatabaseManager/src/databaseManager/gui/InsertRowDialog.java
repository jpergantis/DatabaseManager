package databaseManager.gui;

import databaseManager.managers.DatabaseManager;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.Font;

@SuppressWarnings("serial")
public class InsertRowDialog extends JFrame {
	
	private String tableName;
	private DatabaseManager dbm;
	private String[] columnNames;
	private JTextField[] textFields;
	ArrayList<Boolean> requiredFields;
	
	
	public InsertRowDialog(DatabaseManager databaseManager, String table) {
		
		dbm = databaseManager;
		tableName = table;
		
		setSize(300, 500);
		setTitle("Add Row");
				
		columnNames = null;		
		
		try {
			columnNames = dbm.getColNames(tableName);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		
		requiredFields = null;
		try {
			requiredFields = dbm.getRequiredColumns(table);
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
				
		for (int x = 0; x < columnNames.length; x++) {
			if (requiredFields.get(x))
				columnNames[x] += " * ";
		}
		
		int numRows = columnNames.length; // 1 row for each field
		
		textFields = new JTextField[numRows];
		for (int x = 0; x < textFields.length; x++) {
			textFields[x] = new JTextField(15);
		}

		// Create and populate the panel
		JPanel panel = new JPanel(new SpringLayout());
		for (int x = 0; x < numRows; x++) {
		    JLabel l = new JLabel(columnNames[x], JLabel.TRAILING);
		    panel.add(l);
		    JTextField textField = textFields[x];
		    
		    l.setLabelFor(textField);
		    panel.add(textField);
		}

		// Lay out the panel
		SpringUtilities.makeCompactGrid(panel,
		                                numRows, 2, //rows, cols
		                                6, 6,        //initX, initY
		                                6, 6);       //xPad, yPad
		
		// getContentPane().add(panel);
		JScrollPane scrollPane = new JScrollPane(panel);
		getContentPane().add(scrollPane);
		
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		// Send the contents of all text fields (even empty ones) to the DatabaseManager to parse and insert into the database
		JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Generate list of values
				String[] textFieldContents = new String[numRows];
				for (int x = 0; x < textFields.length; x++) {
					textFieldContents[x] = textFields[x].getText();
				}
				
				// Validate that all necessary values have been filled
				// Additionally, validate that all of the necessary values are of the correct type
				boolean emptyRequiredFields = false;
				boolean invalidDataTypes = false;
				String missingFields = "The following fields are required: \n";
				String invalidFields = "The following fields have invalid data types: \n";

				
				for (int x = 0; x < textFieldContents.length; x++) {
					if (textFieldContents[x].isEmpty() && requiredFields.get(x)) { // If the field is empty but it is required, don't accept the input
						missingFields += columnNames[x].substring(0, columnNames[x].length() - 2) + "\n"; // Add the required field to the list of missing ones (with the asterisk trimmed off)
						emptyRequiredFields = true;
					}
					if (!(parseInput(textFieldContents[x]).getClass().equals(dbm.getColumnTypes(table)[x])) && !textFieldContents[x].isEmpty()) {
						
						invalidFields += columnNames[x].substring(0, columnNames[x].length() - 2) + "\n"; // Add the required field to the list of missing ones (with the asterisk trimmed off)
						invalidDataTypes = true;
					}
				}
				
				// Create the error message, if necessary
				String fullErrorMsg = "";
				if (emptyRequiredFields) {
					fullErrorMsg += missingFields + "\n\n";
				}
				if (invalidDataTypes) {
					fullErrorMsg += invalidFields;
				}
				
				// If any input data is invalid/missing, show an error message informing the user.
				// Otherwise, pass the data to DatabaseManager to add to the table.
				if (emptyRequiredFields || invalidDataTypes) {
					showDialog(fullErrorMsg);
				} else {
					try {
						dbm.addRowToTable(table, textFieldContents);
						dispose();
					} catch (SQLException e1) {
						// Print an error message/dialog if there is something wrong with the user's input
						e1.printStackTrace();
					}
				}			
			}
			
		});
		buttonPanel.add(submitButton);
		
		// Cancel the operation
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
			
		});
		buttonPanel.add(cancelButton);
		
		JPanel infoPanel = new JPanel();
		getContentPane().add(infoPanel, BorderLayout.NORTH);
		
		JLabel requiredFieldsLabel = new JLabel("* indicates required field");
		requiredFieldsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
		infoPanel.add(requiredFieldsLabel);
		
		
		setVisible(true);
		
	}
	
	private Object parseInput(String s) {
		/**
		 * TODO 
		 * This is not really a great way to prepare the query, it should parse based on the type of column of colParam
		 * In fact it would probably be most appropriate to create a separate method or even a class for preparing queries, especially to do so more safely
		 */
		// Prepare the query
		Object parsedTextParam = null;
		try {
			parsedTextParam = Integer.parseInt(s); // Try it as an integer
		}
		catch (NumberFormatException e1) {
			try {
				parsedTextParam =  Double.parseDouble(s); // If it isn't an integer, try as a double
			}
			catch (NumberFormatException e2) {
				parsedTextParam = s; // Otherwise assume it's a string
			}
		}
			
		return parsedTextParam;
	}
	
	private void showDialog(String text) {
		JOptionPane.showMessageDialog(this, text);
	}
}
