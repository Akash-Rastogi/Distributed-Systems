package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import gui.ClientGUI;

/**
 * @author akashrastogi
 *
 */
public class Client implements Runnable {
	static Socket clientSocket = null;
	static BufferedWriter os = null;
	static BufferedReader is = null;
	static BufferedReader inputLine = null;
	static boolean closed = false;
	static ClientGUI clientGUI;
	static String messageToWrite;
	Timer timer;
	int timerWait = 15000;
	boolean ackFlag = false;

	static String name = "Amber"; // Name of client

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Create UI
		clientGUI = new ClientGUI(name);
		
		// Display history
		displayHistory();
		
		// Add Event Listeners
		addEventListeners();

		// Try connetion
		try {
			clientSocket = new Socket("localhost", 1111);
			inputLine = new BufferedReader(new InputStreamReader(System.in));

			// Creating output and input streams
			os = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
			is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (Exception e) {
			System.out.println("Exception occurred : " + e.getMessage());
		}
		if (clientSocket != null && os != null && is != null) {
			try {
				new Thread(new Client()).start();
				while (!closed) {
					os.write(inputLine.readLine());
				}
				
				// close all streams
				os.close();
				is.close();
				
				// close socket
				clientSocket.close();
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

			// Adding HTTP header
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
			// Decode encoded message
			String decoded = URLDecoder.decode(line, "UTF-8");
			String nameAndMsg = decoded.split("from=")[1];
			
			// Return msg and sender's name
			return new String[]{ nameAndMsg.split("&msg=")[0] , nameAndMsg.split("&msg=")[1] };
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new String[]{ "Error" , "Cannot decode the msg" };
		}
	}

	/**
	 * Display history from file
	 */
	private static void displayHistory() {
		String filename = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
				+ File.separator + name + ".txt";

		// Create file object
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
				
				// Close reader
				br.close();
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * GUI event handling
	 */
	private static void addEventListeners() {
		
		// Disable UI button
		clientGUI.abortBtn.setEnabled(false);
		clientGUI.commitBtn.setEnabled(false);

		// Abort Button Action Listener
		clientGUI.abortBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMsg(name, "ABORTED");
				
				// Disable UI buttons
				clientGUI.abortBtn.setEnabled(false);
				clientGUI.commitBtn.setEnabled(false);
			}
		});

		// Commit Button Action Listener
		clientGUI.commitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If recived message is Prepare - Commit
				if(clientGUI.commitBtn.getText().contains("Prepare-Commit")) {
					clientGUI.commitBtn.setText("Commit");
					sendMsg(name, "Pre-COMMITTED");
					
					// Enable UI buttons
					clientGUI.abortBtn.setEnabled(true);
					clientGUI.commitBtn.setEnabled(true);
				} 
				
				// If recived message is not Prepare - Commit
				else {
					sendMsg(name, "COMMITTED");
					
					// Disable UI buttons
					clientGUI.abortBtn.setEnabled(false);
					clientGUI.commitBtn.setEnabled(false);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		String responseLine;
		TimerTask timerTask = null;
		
		try {
			while (is != null && (responseLine = is.readLine()) != null) {
				
				// Decode message
				String[] fromAndMsg = readMsg(responseLine);
				String from = fromAndMsg[0];
				String msg = fromAndMsg[1];
				
				clientGUI.area.append(from + " : " + msg + "\n");
				
				// If coordinator sends ACK
				if(from.equalsIgnoreCase("Coordinator") && msg.equalsIgnoreCase("ACK")) {
					// Set ACK flag
					ackFlag = true;
					
					// Change button text
					clientGUI.commitBtn.setText("ACK");
					
					// Disable abort button
					clientGUI.abortBtn.setEnabled(false);
					
					// Cancel old timer and resets to timerWait
					timer.cancel();
			        timer = new Timer();
			        timerTask = new TimerTask() {

			            @Override
			            public void run() {
			            	// If ACK is not done
			            	if(!ackFlag)
			            		clientGUI.abortBtn.doClick();
			            	else
			            		sendMsg(name, "COMMITTED");
			            	
			            	// Disable UI buttons
							clientGUI.abortBtn.setEnabled(false);
							clientGUI.commitBtn.setEnabled(false);
			            }
			        };
			        timer.schedule(timerTask, timerWait);
				}
				
				// If GLOBAL_COMMIT
				if (msg.equalsIgnoreCase("GLOBAL_COMMIT")) {
					
					// Write msg to file
					String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
							+ File.separator;
					BufferedWriter bw = new BufferedWriter(new FileWriter(dir + name + ".txt", true));
					bw.write(messageToWrite);
					
					// Disable UI buttons
					clientGUI.abortBtn.setEnabled(false);
					clientGUI.commitBtn.setEnabled(false);
					
					// Flush and close the writer
					bw.flush();
					bw.close();
					
					// Break from loop
					break;
				} 
				
				// If GLOBAL_ABORT
				else if (msg.equalsIgnoreCase("GLOBAL_ABORT") == true) {
					closed = true;
					
					// Disable UI buttons
					clientGUI.abortBtn.setEnabled(false);
					clientGUI.commitBtn.setEnabled(false);
					break;
				} 
				
				// Setting message to wirte in File
				else if (messageToWrite == null || messageToWrite.length() == 0) {
					
					// Enable abort/commit buttons
					clientGUI.abortBtn.setEnabled(true);
					clientGUI.commitBtn.setEnabled(true);
					
					// Save msg to finally write
					messageToWrite = msg;
					
					timerTask = new TimerTask() {

			            @Override
			            public void run() {
			            	// If ACK is not done
			            	if(!ackFlag)
			            		clientGUI.abortBtn.doClick();
			            	else
			            		sendMsg(name, "COMMITTED");
			            	
			            	// Disable UI buttons
							clientGUI.abortBtn.setEnabled(false);
							clientGUI.commitBtn.setEnabled(false);
			            }
			        };
			        
			        timer = new Timer();
			        timer.schedule(timerTask, timerWait);
				}
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}
	}
}