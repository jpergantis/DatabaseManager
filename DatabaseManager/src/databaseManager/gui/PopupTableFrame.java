package databaseManager.gui;

import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import databaseManager.managers.DatabaseManager;

import java.awt.Choice;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class PopupTableFrame extends JFrame {

	private JPanel contentPane;
	private JScrollPane scrollPane;
	JPanel menuPanel;
	JTable table;
	
	private TextField searchTerm;
	JButton searchButton;
	JButton resetButton;
	Choice searchColSelector;
	
	String lastValidSearchTerm = "*";

	/**
	 * Create the frame.
	 */
	public PopupTableFrame(DatabaseManager dbm, String tableName) throws SQLException {
		
		table = dbm.viewTable(tableName);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		scrollPane = new JScrollPane(table);
		scrollPane.setBounds(5, 5, 774, 449);
		table.setFillsViewportHeight(true);
		contentPane.setLayout(null);
		
		contentPane.add(scrollPane);
		
		menuPanel = new JPanel();
		menuPanel.setBounds(5, 454, 774, 102);
		contentPane.add(menuPanel);
		menuPanel.setLayout(null);
		
		searchColSelector = new Choice();
		searchColSelector.setBounds(156, 35, 128, 20);
		String[] colNames = dbm.getColNames(tableName);
		for (String s : colNames) {
			searchColSelector.add(s);
		}
		
		menuPanel.add(searchColSelector);
		
		searchTerm = new TextField();
		searchTerm.setBounds(303, 35, 145, 20);
		menuPanel.add(searchTerm);
		
		searchButton = new JButton("Search");
		searchButton.setBounds(478, 35, 100, 20);
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub;
				contentPane.remove(scrollPane);
				try {
					scrollPane = new JScrollPane(dbm.viewTable(tableName, searchColSelector.getItem(searchColSelector.getSelectedIndex()), parseSearchTerm()));
					lastValidSearchTerm = parseSearchTerm();
					scrollPane.setBounds(5, 5, 774, 449);
					table.setFillsViewportHeight(true);
					contentPane.add(scrollPane);
					repaint();
				} 
				catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
		});
		
		menuPanel.add(searchButton);
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				contentPane.remove(scrollPane);
				scrollPane = new JScrollPane(table);
				scrollPane.setBounds(5, 5, 774, 449);
				table.setFillsViewportHeight(true);
				contentPane.setLayout(null);			
				contentPane.add(scrollPane);
				searchTerm.setText("");
			}
		});
		resetButton.setBounds(593, 35, 100, 20);
		menuPanel.add(resetButton);
		
		setSize(800, 600);
		setVisible(true);	
	}
	
	private String parseSearchTerm() {
		String result = null;
		try {
			Integer number = Integer.parseInt(searchTerm.getText());
			result = number.toString();
		}
		catch (NumberFormatException e) {
			result = "\"" + searchTerm.getText() + "\"";
		}
		return result;
	}
}
