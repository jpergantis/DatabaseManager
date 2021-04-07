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
import java.awt.BorderLayout;
import javax.swing.BoxLayout;

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
	

	/**
	 * Create the frame.
	 */
	public PopupTableFrame(DatabaseManager dbm, String tableName) throws SQLException {
		
		table = dbm.viewTable(tableName);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		contentPane.add(scrollPane);
		
		menuPanel = new JPanel();
		menuPanel.setSize(600, 200);
		contentPane.add(menuPanel, BorderLayout.SOUTH);
		
		searchColSelector = new Choice();
		String[] colNames = dbm.getColNames(tableName);
		for (String s : colNames) {
			searchColSelector.add(s);
		}
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
		
		menuPanel.add(searchColSelector);
		
		searchTerm = new TextField();
		menuPanel.add(searchTerm);
		
		searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub;
				contentPane.remove(scrollPane);
				try {
					int oldWidth = scrollPane.getWidth(); // Save the original dimensions of the old scrollPane to use for the new one
					int oldHeight = scrollPane.getHeight();
					scrollPane = new JScrollPane(dbm.viewTable(tableName, searchColSelector.getItem(searchColSelector.getSelectedIndex()), searchTerm.getText()));
					scrollPane.setBounds(5, 5, oldWidth, oldHeight);
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
		menuPanel.add(resetButton);
		
		setSize(800, 600);
		setVisible(true);	
	}
	
}
