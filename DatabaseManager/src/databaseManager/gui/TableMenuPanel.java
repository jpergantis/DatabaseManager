/**
 * The TablesMenu class provides a JPanel object to display buttons used to access the available tables in a database.
 * Each button will activate a PopupTable for the selected database table.
 */

package databaseManager.gui;

import javax.swing.JButton;
import javax.swing.JPanel;

import databaseManager.managers.DatabaseManager;

import java.awt.BorderLayout;
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
						new PopupTableFrame(dbm, j.getText());
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


