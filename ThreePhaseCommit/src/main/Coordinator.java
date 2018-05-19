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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;

import gui.CoordinatorGUI;

/**
 * @author akashrastogi
 *
 */
public class Coordinator implements Runnable {
	static Socket clientSocket = null;
	static PrintWriter os = null;
	static DataInputStream is = null;
	static BufferedReader inputLine = null;
	static boolean closed = false;
	static CoordinatorGUI coordinatorGUI;
	int commit_count = 0;
	static String name = "Coordinator";
	static boolean ackFlag = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		coordinatorGUI = new CoordinatorGUI();
		addEventListeners();
		displayHistory();

		// Try connection
		try {
			clientSocket = new Socket("localhost", 1111);
			inputLine = new BufferedReader(new InputStreamReader(System.in));

			os = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
			is = new DataInputStream(clientSocket.getInputStream());
		} catch (Exception e) {
			System.out.println("Exception occurred : " + e.getMessage());
		}

		// if not null, start the thread
		if (clientSocket != null && os != null && is != null) {
			try {
				
				// Creating new thread
				new Thread(new Coordinator()).start();
				while (!closed) {
					os.write(inputLine.readLine());
				}
				
				// close output stream and input stream
				os.close();
				is.close();
				
				// close socket
				clientSocket.close();
				
				// Handle IO Exception
			} catch (IOException e) {
				System.err.println("IOException: " + e);
			}
		}
	}

	/**
	 * @param from
	 * @param msg
	 */
	private static void sendMsg(String from, String msg) {
		String params;
		try {
			
			// Adding params
			params = URLEncoder.encode("from", "UTF-8") + "=" + URLEncoder.encode(from, "UTF-8");
			params += "&" + URLEncoder.encode("msg", "UTF-8") + "=" + URLEncoder.encode(msg, "UTF-8");

			// Adding headers
			os.write("POST localhost:1111 HTTP/1.0rn");
			os.write("Content-Length: " + params.length() + "rn");
			os.write("Content-Type: application/x-www-form-urlencodedrn");
			os.write("rn");
			os.write(params + "\n");
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param line
	 * @return String[]
	 */
	private static String[] readMsg(String line) {
		try {
			
			// URL Decoding
			String decoded = URLDecoder.decode(line, "UTF-8");
			String nameAndMsg = decoded.split("from=")[1];
			return new String[] { nameAndMsg.split("&msg=")[0], nameAndMsg.split("&msg=")[1] };
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// Return Error if not able to decode
			return new String[] { "Error", "Cannot decode the msg" };
		}
	}

	/**
	 * Method to display history from file
	 */
	private static void displayHistory() {
		String filename = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
				+ File.separator + name + ".txt";

		// Creating file object
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

	/**
	 * Method to add Events
	 */
	private static void addEventListeners() {
		coordinatorGUI.txtField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {

				// Key pressed means, the user is typing
				coordinatorGUI.label.setText("You are typing..");

				// If he presses enter, add text to chat textarea
				if (ke.getKeyCode() == KeyEvent.VK_ENTER && coordinatorGUI.txtField.getText().length() > 0) {
					sendMsg(name, coordinatorGUI.txtField.getText());

					coordinatorGUI.label.setText("");
					coordinatorGUI.txtField.setText("");
					coordinatorGUI.txtField.setEnabled(false);

					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							if(ackFlag)
								sendMsg(name, "GLOBAL_COMMIT");
							else
								sendMsg(name, "GLOBAL_ABORT");
						}
					}, 20000); // 20 sec
				}
			}

			public void keyReleased(KeyEvent ke) {
				// When the user isn't typing..
				coordinatorGUI.label.setText("");
			}
		});
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		int numOfClient = 3;
		String responseLine;
		
		String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs" + File.separator;
		// Flag to keep track of ACK
		
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(dir + name + ".txt", true));
			while (is != null && (responseLine = is.readLine()) != null) {
				// Read msg
				String[] fromAndMsg = readMsg(responseLine);
				String from = fromAndMsg[0];
				String msg = fromAndMsg[1];

				coordinatorGUI.area.append(from + " : " + msg + "\n");
				
				// If message is ABORTED
				if (msg.equals("ABORTED")) {
					sendMsg(name, "GLOBAL_ABORT");

					coordinatorGUI.area.append("GLOBAL_ABORT");
					clientSocket.close();
					break;
				} 
				// If message is COMMITTED
				else if (msg.contains("COMMITTED")) {
					commit_count++;
				}

				// Check for ACK counts
				if (commit_count == numOfClient && !ackFlag) {
					ackFlag = true;
					sendMsg(name, "ACK");
//					coordinatorGUI.area.append("ACK");
				} 
				// Check for COMMIT counts
				else if (commit_count == numOfClient*2) {
					sendMsg(name, "GLOBAL_COMMIT");
					coordinatorGUI.area.append("GLOBAL_COMMIT");
					clientSocket.close();
					break;
				}
				bw.write(msg + "\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}

	}
}