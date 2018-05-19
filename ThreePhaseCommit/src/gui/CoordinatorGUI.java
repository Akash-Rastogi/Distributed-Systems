package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


// Coordinators GUI
public class CoordinatorGUI extends JFrame {
	public JPanel panel;
	public JTextField txtField;
	public JTextArea area;
	public JLabel label;

	public CoordinatorGUI() {
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		// Set frame properties
		setTitle("Coordinator");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Create a JPanel and set layout
		panel = new JPanel(new GridLayout(2, 1, 5, 5));
		// panel.setLayout(new GridLayout(2, 1));
		label = new JLabel();
		panel.add(label).setLocation(0, 0);

		// Create JTextField, add it.
		txtField = new JTextField(50);
		panel.add(txtField);
		
		txtField.setEnabled(true);

		// Add panel to the south,
		add(panel, BorderLayout.SOUTH);

		// Create a textarea
		area = new JTextArea();

		// Make it non-editable
		area.setEditable(false);

		// Text Wrapping
		area.setLineWrap(true);

		// Set some margin, for the text
		area.setMargin(new Insets(7, 7, 7, 7));

		// Set a scrollpane
		JScrollPane scroll = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(scroll);

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent we) {
				// Get the focus when window is opened
				txtField.requestFocus();
			}
		});

		setSize(600, 400);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CoordinatorGUI();
			}
		});
	}
}