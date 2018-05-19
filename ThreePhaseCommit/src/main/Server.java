package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import gui.ServerGUI;

/**
 * @author akashrastogi
 *
 */
public class Server {
	boolean closed = false, inputFromAll = false;
	static public List<ClientThread> clientThreads;
	ServerGUI serverGUI;
	ServerSocket serverSocket;

	/**
	 * Server Constructor
	 */
	Server() {
		// List to keep all client threads
		clientThreads = new ArrayList<ClientThread>();
		
		// Creating Server UI
		serverGUI = new ServerGUI();
		
		// call to Display History
		displayHistory();
	}

	// SetUP server
	private void setUp() {
		try {
			// Create Server Socket
			serverSocket = new ServerSocket(1111);

			while (!closed) {
				
				// Accept Connections
				Socket clientSocket = serverSocket.accept();

				// Create client thread
				ClientThread clientThread = new ClientThread(this, clientSocket);

				// Add to list
				clientThreads.add(clientThread);

				// Print number of clients Info
				serverGUI.print("Now Total clients are : " + clientThreads.size() + "\n");

				clientThread.start();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * Display History from file
	 */
	private void displayHistory() {
		String filename = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
				+ File.separator + "Server" + ".txt";

		// Creating file object to save logs
		File f = new File(filename);

		// This will reference one line at a time
		String line = null;

		try {
			// If file does not exists create new file
			if (!f.exists() && !f.isDirectory())
				f.createNewFile();
			else {
				// FileReader reads text files in the default encoding.
				BufferedReader br = new BufferedReader(new FileReader(filename));

				serverGUI.area.append("******************* HISTORY *******************" + "\n");
				while ((line = br.readLine()) != null) {
					serverGUI.area.append(line + "\n");
				}
				serverGUI.area.append("******************* HISTORY *******************" + "\n");
				br.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		Server server = new Server();
		server.setUp();
	}
}

/**
 * @author akashrastogi
 * Class to maintain client threads
 */
class ClientThread extends Thread {
	Server server;
	BufferedReader rd = null;
	String line;
	PrintStream printStream = null;
	Socket clientSocket = null;
	boolean closed = false;

	/**
	 * @param ser
	 * @param clientSocket
	 */
	public ClientThread(Server ser, Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.server = ser;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			printStream = new PrintStream(clientSocket.getOutputStream());
			
			
			String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
					+ File.separator;
			BufferedWriter bw = new BufferedWriter(new FileWriter(dir + "Server" + ".txt", true));

			// While not closed. Closed is a boolean variable
			while (!closed) {
				line = rd.readLine();
				bw.write(line + "\n");

				if (line != null && line.length() > 0) {
					server.serverGUI.area.append(line + "\n");

					for (int i = 0; i < server.clientThreads.size(); i++)
						server.clientThreads.get(i).printStream.println(line);
				}

				if (line.equals("GLOBAL_COMMIT") || line.equals("GLOBAL_ABORT")) {
					closed = true;
				}

				bw.flush();
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
