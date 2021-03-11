/**
 * The TablesMenu class provides a JPanel object to display buttons used to access the available tables in a database.
 * Each button will activate a PopupTable for the selected database table.
 */

package databaseManager.gui;

import javax.swing.JButton;
import javax.swing.JPanel;

import databaseManager.managers.DatabaseManager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;



@SuppressWarnings("serial")
public class TableMenuPanel extends JPanel {

	DatabaseManager dbm;
	
	/**
	 * Create the panel.
	 * @throws SQLException 
	 */
	public TableMenuPanel(DatabaseManager d) throws SQLException {
		dbm = d;
		
		setSize(800, 600);

		ArrayList<String> tableNames = dbm.getTables();
		
		int rows = (int) Math.ceil(tableNames.size() / 1.69); // Golden ratio 
		int cols = (int) tableNames.size() / rows;
		
		GridLayout layout = new GridLayout(rows, cols);
		layout.setVgap(50);
		layout.setHgap(50);
		// setLayout(layout);
		
		// Generate the labels for each button
		ArrayList<JButton> buttonList = new ArrayList<JButton>();
		for (String s : tableNames) {
			buttonList.add(new JButton(s));
		}
		
		// Add the buttons and assign them to access the relevant tables
		for (JButton j : buttonList) {
			j.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						new PopupTableFrame(dbm.viewTable(j.getText()));
					} 
					catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}	
			});
			
			add(j, BorderLayout.CENTER);
		}
	}
}


