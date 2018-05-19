package gui;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


// Clients GUI
public class ClientGUI extends JFrame {
	public JPanel panel;
	public JTextArea area;
	public JButton commitBtn, abortBtn;

	public ClientGUI(String title) {
		createAndShowGUI(title);
	}

	private void createAndShowGUI(String title) {
		// Set frame properties
		setTitle(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Initializing buttons
		commitBtn = new JButton("Prepare-Commit");
		abortBtn = new JButton("Abort");

		// Create a JPanel and set layout
		panel = new JPanel();

		panel.add(commitBtn, BorderLayout.LINE_START);
		panel.add(abortBtn, BorderLayout.LINE_END);

		commitBtn.setEnabled(true);
		abortBtn.setEnabled(true);

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

		setSize(400, 400);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void showLabel(String text) {
		// If text is empty return
		if (text.trim().isEmpty())
			return;

		// Otherwise, append text with a new line
		area.append(text + "\n");
		System.out.println(text);

		// Set textfield and label text to empty string
		area.setEnabled(false);
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ClientGUI("Client");
			}
		});
	}
}