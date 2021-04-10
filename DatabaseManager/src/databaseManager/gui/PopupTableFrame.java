/**
 * Displays JTables derived from SQL queries in DatabaseManager in table form
 * Allows user to safely search for entries in the table
 * Stores previously viewed tables on a stack for back/forward functionality
 */

package databaseManager.gui;

import java.sql.SQLException;
import java.util.Stack;

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
import java.awt.Font;
import java.awt.FlowLayout;

@SuppressWarnings("serial")
public class PopupTableFrame extends JFrame {

	private JPanel contentPane;
	private JScrollPane scrollPane;
	private JPanel menuPanel;
	private JTable currentTable;
	
	private TextField searchTerm;
	private JButton searchButton;
	private JButton resetButton;
	private Choice searchColSelector;
	
	private JPanel buttonPanel;
	private JButton backButton;
	private JButton fwdButton;
	
	private DatabaseManager dbm;
	private String tableName;
	
	private Stack<JScrollPane> backStack; // Contains earlier viewed tables relative to current table
	private Stack<JScrollPane> fwdStack; // Contains later viewed tables relative to current table
	

	/**
	 * Create the frame.
	 */
	public PopupTableFrame(DatabaseManager databaseManager, String table) throws SQLException {
		
		dbm = databaseManager;
		tableName = table;
		backStack = new Stack<JScrollPane>();
		fwdStack = new Stack<JScrollPane>();
		
		currentTable = dbm.viewTable(tableName);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		scrollPane = new JScrollPane(currentTable);
		currentTable.setFillsViewportHeight(true);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		menuPanel = new JPanel();
		contentPane.add(menuPanel, BorderLayout.SOUTH);
		
		searchColSelector = new Choice();
		searchColSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		String[] colNames = dbm.getColNames(tableName);
		for (String s : colNames) {
			searchColSelector.add(s);
		}
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
		
		menuPanel.add(searchColSelector);
		
		searchTerm = new TextField();
		searchTerm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		
		// Cause the search function to be executed on pressing enter
		searchTerm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performSearch();
			}
		});
		
		menuPanel.add(searchTerm);
		
		searchButton = new JButton("Search");
		searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				performSearch();
			}
			
		});
		
		menuPanel.add(searchButton);
		
		resetButton = new JButton("Reset");
		resetButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub;
				contentPane.remove(scrollPane);
				backStack.push(scrollPane);
				backButton.setEnabled(true);
				try {
					scrollPane = new JScrollPane(dbm.viewTable(tableName));
				} 
				catch (SQLException e1) {
					e1.printStackTrace();
				}
				currentTable.setFillsViewportHeight(true);
				contentPane.add(scrollPane);
				contentPane.revalidate();
				contentPane.repaint();
			}
			
		});
		
		menuPanel.add(resetButton);
		
		buttonPanel = new JPanel();
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		// TODO add button functionality for all buttons
		backButton = new JButton("Back");
		backButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contentPane.remove(scrollPane);
				fwdStack.push(scrollPane); // Put the current JScrollPane on the fwdstack so we can get back to it
				scrollPane = new JScrollPane(backStack.pop()); // Fetches the most recent JScrollPane from the backStack
				if (backStack.isEmpty()) {
					backButton.setEnabled(false);
				}
				fwdButton.setEnabled(true);
				currentTable.setFillsViewportHeight(true);
				contentPane.add(scrollPane);
				contentPane.revalidate();
				contentPane.repaint();
			}
		});
		
		backButton.setEnabled(false);
		buttonPanel.add(backButton);
		
		fwdButton = new JButton("Forward");
		fwdButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fwdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contentPane.remove(scrollPane);
				backStack.push(scrollPane); // Put the current JScrollPane on the backStack so we can get back to it
				scrollPane = new JScrollPane(fwdStack.pop()); // Fetches the most recent JScrollPane from the fwdStack
				if (fwdStack.isEmpty()) {
					fwdButton.setEnabled(false);
				}
				backButton.setEnabled(true);
				currentTable.setFillsViewportHeight(true);
				contentPane.add(scrollPane);
				contentPane.revalidate();
				contentPane.repaint();
			}
		});
		
		fwdButton.setEnabled(false);
		buttonPanel.add(fwdButton);
		
		JButton addRowButton = new JButton("Add Row");
		addRowButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		addRowButton.setEnabled(false);
		buttonPanel.add(addRowButton);
		
		JButton addColButton = new JButton("Add Column");
		addColButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		addColButton.setEnabled(false);
		buttonPanel.add(addColButton);
		
		setSize(800, 600);
		setVisible(true);	
	}
	
	// Performs the search and update operations for both the search button and search box enter key to prevent redundant code
	private void performSearch() {
		contentPane.remove(scrollPane);
		if (backStack.isEmpty()) {
			fwdStack = new Stack<JScrollPane>(); // Resets the forward/backward upon returning to the original table and performing a new search (when both stacks are empty, you must be at the beginning)
			fwdButton.setEnabled(false);
		}
		backStack.push(scrollPane);
		backButton.setEnabled(true);
		try {
			scrollPane = new JScrollPane(dbm.viewTable(tableName, searchColSelector.getItem(searchColSelector.getSelectedIndex()), searchTerm.getText()));	
		} 
		catch (SQLException e1) {
			e1.printStackTrace();
		}
		currentTable.setFillsViewportHeight(true);
		contentPane.add(scrollPane);
		contentPane.revalidate();
		contentPane.repaint();
	}
}
