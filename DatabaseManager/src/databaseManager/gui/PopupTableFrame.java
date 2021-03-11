package databaseManager.gui;

import java.awt.BorderLayout;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class PopupTableFrame extends JFrame {

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public PopupTableFrame(JTable table) throws SQLException {
			
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		
		getContentPane().add(scrollPane);
		
		setSize(450, 300);
		setVisible(true);	
	}

}
