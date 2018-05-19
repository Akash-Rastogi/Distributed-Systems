package gui;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// Servers GUI
public class ServerGUI {
	JFrame frame;
	public JTextArea area;

	public ServerGUI() {
		// Main frame
		frame = new JFrame("Server");
		area = new JTextArea();
		
		// Setting line wrap
		area.setLineWrap(true);
		area.setEditable(false);
		
		// Setting size
		area.setBounds(20, 20, 550, 250);
		
		// Making scrollable pane
		JScrollPane scroll = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		frame.add(scroll);
		
		// Frame's size
		frame.setSize(600, 300);
		
		// Make visible
		frame.setVisible(true);
	}

	// Method to print on main area
	public void print(String msg) {
		area.append(msg + "\n");
	}

	// Destroy UI.
	public void destroUI() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	// Main method to test UI
	public static void main(String[] args) {
		ServerGUI ui = new ServerGUI();
	}
}
