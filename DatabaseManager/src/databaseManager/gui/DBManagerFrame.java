package databaseManager.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import databaseManager.managers.DatabaseManager;

import java.awt.BorderLayout;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class DBManagerFrame extends JFrame {

	private JPanel contentPane;
	private DatabaseManager dbm = new DatabaseManager();
	private TableMenuPanel tmp;
	private JMenuBar menuBar;
	private JMenu mnNewMenu;
	private JMenuItem openDBMenuItem;
	private JPanel bodyPanel;
	private JLabel titleLabel;
	private JPanel headerPanel;
	private JPanel footerPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
	
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DBManagerFrame frame = new DBManagerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public DBManagerFrame() {
		setTitle("Database Manager");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		
		contentPane = new JPanel();
		contentPane.setBounds(new Rectangle(0, 0, 100, 100));
		contentPane.setFont(new Font("Arial", Font.PLAIN, 12));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		bodyPanel = new JPanel();
		bodyPanel.setBounds(5, 29, 774, 423);
		contentPane.add(bodyPanel);
		bodyPanel.setLayout(new BorderLayout(0, 0));
		
		headerPanel = new JPanel();
		headerPanel.setBounds(5, 5, 774, 24);
		contentPane.add(headerPanel);
		
		titleLabel = new JLabel("Welcome to DBManager. Select File > Open Existing Database to begin.");
		headerPanel.add(titleLabel);
		
		footerPanel = new JPanel();
		footerPanel.setBounds(5, 452, 774, 82);
		contentPane.add(footerPanel);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		
		mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		

		
		// Lets the user select a database from the filesystem
		openDBMenuItem = new JMenuItem("Open Existing Database");
		openDBMenuItem.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectFile();
				// Go to the TablesMenu containing buttons for each table in the database
				try {
					if (tmp != null) {
						contentPane.remove(tmp); // Remove the old TableMenuPanel and update it with a new one
					}
						
					tmp = new TableMenuPanel(dbm);
					bodyPanel.add(tmp, BorderLayout.CENTER);
					titleLabel.setText(dbm.getFileName() + " selected. Select a table to view"); 
					
					// For some reason simply changing the panel and calling repaint does not correctly update the table
					setSize((int) getSize().getHeight() + 1, (int) getSize().getWidth());
					setSize((int) getSize().getHeight() - 1, (int) getSize().getWidth());

				} catch (SQLException e1) {
					System.out.println("Unable to fetch tables");
					e1.printStackTrace();
				}
			}
		});
		mnNewMenu.add(openDBMenuItem);				
	}
	
	private void selectFile() {
		
		// Create file chooser
		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Database files", "db");
		jfc.setFileFilter(filter);
		jfc.showOpenDialog(getParent());
		
		// Get and save the file
		dbm.setDbFile(jfc.getSelectedFile());
	}
}
