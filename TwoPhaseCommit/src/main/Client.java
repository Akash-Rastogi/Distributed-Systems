package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import gui.ClientGUI;

public class Client implements Runnable {
	static Socket clientSocket = null;
	static PrintStream os = null;
	static DataInputStream is = null;
	static BufferedReader inputLine = null;
	static boolean closed = false;
	static ClientGUI clientGUI;
	static String messageToWrite;
	
	static String name = "Amber"; // Name of client

	// Main method
	public static void main(String[] args) {

		clientGUI = new ClientGUI(name);
		displayHistory();
		addEventListeners();

		// Try connetion
		try {
			clientSocket = new Socket("localhost", 1111);
			inputLine = new BufferedReader(new InputStreamReader(System.in));

			os = new PrintStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
		} catch (Exception e) {
			System.out.println("Exception occurred : " + e.getMessage());
		}
		if (clientSocket != null && os != null && is != null) {
			try {
				new Thread(new Client()).start();
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
	
	// Display history from file
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
				clientGUI.area.append("******************* HISTORY *******************" + "\n");

				while ((line = br.readLine()) != null) {
					clientGUI.area.append(line + "\n");
				}
				clientGUI.area.append("******************* HISTORY *******************" + "\n");
				br.close();
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// GUI event handling
	private static void addEventListeners() {
		clientGUI.abortBtn.setEnabled(false);
		clientGUI.commitBtn.setEnabled(false);

		clientGUI.abortBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				os.println("\n" + name + ": ABORTED");
				clientGUI.abortBtn.setEnabled(false);
				clientGUI.commitBtn.setEnabled(false);
			}
		});

		clientGUI.commitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				os.println("\n" + name + ": COMMITTED");
				clientGUI.abortBtn.setEnabled(false);
				clientGUI.commitBtn.setEnabled(false);
			}
		});
	}

	// Thread run method
	public void run() {
		String responseLine;
		try {
			while (is != null && (responseLine = is.readLine()) != null) {
				clientGUI.area.append(responseLine + "\n");

				if (responseLine.contains("Coordinator")) {
					clientGUI.abortBtn.setEnabled(true);
					clientGUI.commitBtn.setEnabled(true);
				}
				if (responseLine.equalsIgnoreCase("GLOBAL_COMMIT")) {
					String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
							+ File.separator;
					BufferedWriter bw = new BufferedWriter(new FileWriter(dir + name + ".txt", true));
					bw.write(messageToWrite);
					bw.flush();
					bw.close();
					clientSocket.close();
					break;
				} else if (responseLine.equalsIgnoreCase("GLOBAL_ABORT") == true) {
					closed = true;
					clientSocket.close();
					break;
				} else if (messageToWrite == null || messageToWrite.length() == 0) {
					messageToWrite = responseLine;

					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							clientGUI.abortBtn.doClick();
						}
					}, 15000);
				}
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}
	}
}