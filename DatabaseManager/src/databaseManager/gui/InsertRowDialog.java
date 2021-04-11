package databaseManager.gui;

import databaseManager.managers.DatabaseManager;

import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class InsertRowDialog extends JFrame {
	
	private String tableName;
	private DatabaseManager dbm;
	private String[] columnNames;
	private JTextField[] textFields;
	
	
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
				try {
					dbm.addRowToTable(table, textFieldContents);
					dispose();
				} catch (SQLException e1) {
					// Print an error message/dialog if there is something wrong with the user's input
					e1.printStackTrace();
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
		setVisible(true);
		
	}
	
}
