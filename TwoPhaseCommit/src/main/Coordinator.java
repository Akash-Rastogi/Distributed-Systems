package main;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import gui.CoordinatorGUI;

public class Coordinator implements Runnable {
	static Socket clientSocket = null;
	static PrintStream os = null;
	static DataInputStream is = null;
	static BufferedReader inputLine = null;
	static boolean closed = false;
	static CoordinatorGUI coordinatorGUI;
	int commit_count = 0;
	static String name = "Coordinator";
	
	
	// Main method
	public static void main(String[] args) {
		coordinatorGUI = new CoordinatorGUI();
		addEventListeners();
		displayHistory();

		// Try connection
		try {
			clientSocket = new Socket("localhost", 1111);
			inputLine = new BufferedReader(new InputStreamReader(System.in));

			os = new PrintStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
		} catch (Exception e) {
			System.out.println("Exception occurred : " + e.getMessage());
		}
		
		
		// if not null, start the thread
		if (clientSocket != null && os != null && is != null) {
			try {
				new Thread(new Coordinator()).start();
				while (!closed) {
					os.println(inputLine.readLine());
				}
				os.close();
				is.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e);
			}
		}
	}

	
	// Method to display history from file
	private static void displayHistory() {
		String filename = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
				+ File.separator + name + ".txt";

		File f = new File(filename);

		// This will reference one line at a time
		String line = null;

		try {
			if (!f.exists() && !f.isDirectory())
				f.createNewFile();
			else {
				// FileReader reads text files in the default encoding.
				BufferedReader br = new BufferedReader(new FileReader(filename));

				coordinatorGUI.area.append("******************* HISTORY *******************" + "\n");
				while ((line = br.readLine()) != null) {
					coordinatorGUI.area.append(line + "\n");
				}
				coordinatorGUI.area.append("******************* HISTORY *******************" + "\n");
				br.close();
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method to add Events 
	private static void addEventListeners() {
		coordinatorGUI.txtField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {

				// Key pressed means, the user is typing
				coordinatorGUI.label.setText("You are typing..");

				// If he presses enter, add text to chat textarea
				if (ke.getKeyCode() == KeyEvent.VK_ENTER && coordinatorGUI.txtField.getText().length() > 0) {
					os.println(name + ": " + coordinatorGUI.txtField.getText());

					coordinatorGUI.label.setText("");
					coordinatorGUI.txtField.setText("");
					coordinatorGUI.txtField.setEnabled(false);

					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							os.println("GLOBAL_COMMIT");
						}
					}, 20000);
				}
			}

			public void keyReleased(KeyEvent ke) {
				// When the user isn't typing..
				coordinatorGUI.label.setText("");
			}
		});
	}

	// Thread run
	public void run() {
		String responseLine;
		String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs" + File.separator;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(dir + name + ".txt", true));
			while (is != null && (responseLine = is.readLine()) != null) {
				String msg = responseLine.contains(":") ? responseLine.split(":")[1].trim() : responseLine;
				if (msg.equals("ABORTED")) {
					os.println("GLOBAL_ABORT");
					coordinatorGUI.area.append("GLOBAL_ABORT");
					clientSocket.close();
					break;
				} else if (msg.equals("COMMITTED")) {
					commit_count++;
				}
				if (commit_count == 3) {
					os.println("GLOBAL_COMMIT");
					coordinatorGUI.area.append("GLOBAL_COMMIT");
					clientSocket.close();
					break;
				}
				coordinatorGUI.area.append(responseLine + "\n");
				bw.write(responseLine + "\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}

	}
}