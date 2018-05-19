package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import gui.ServerGUI;

public class Server {
	boolean closed = false, inputFromAll = false;
	static public List<ClientThread> clientThreads;
	List<String> data;
	ServerGUI serverGUI;
	ServerSocket serverSocket;

	// Server Constructor
	Server() {
		clientThreads = new ArrayList<ClientThread>();
		data = new ArrayList<String>();
		serverGUI = new ServerGUI();
		displayHistory();
	}

	// SetUP server
	private void setUp() {
		try {
			serverSocket = new ServerSocket(1111);

			while (!closed) {
				Socket clientSocket = serverSocket.accept();

				ClientThread clientThread = new ClientThread(this, clientSocket);

				clientThreads.add(clientThread);

				serverGUI.print("Now Total clients are : " + clientThreads.size() + "\n");

				data.add("NOT_SENT");

				clientThread.start();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	// Display History from file
	private void displayHistory() {
		String filename = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
				+ File.separator + "Server" + ".txt";

		File f = new File(filename);

		// This will reference one line at a time
		String line = null;

		try {
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

	// Main method
	public static void main(String args[]) {
		Server server = new Server();
		server.setUp();
	}
}


// Class to maintain client threads
class ClientThread extends Thread {
	int id;
	Server server;

	DataInputStream dataInputStream = null;
	String line;
	String destClient = "";
	String name;
	PrintStream printStream = null;
	Socket clientSocket = null;
	String clientIdentity;
	boolean closed = false;

	// Constructor
	public ClientThread(Server ser, Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.server = ser;
	}

	// Thread run method
	public void run() {
		try {
			dataInputStream = new DataInputStream(clientSocket.getInputStream());
			printStream = new PrintStream(clientSocket.getOutputStream());
			String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "logs"
					+ File.separator;
			BufferedWriter bw = new BufferedWriter(new FileWriter(dir + "Server" + ".txt", true));
			
			// While not closed. Closed is a boolean variable
			while (!closed) {
				line = dataInputStream.readLine();

				if (line != null && line.length() > 0) {
					server.serverGUI.area.append(line + "\n");
					bw.write(line + "\n");

					for (int i = 0; i < server.clientThreads.size(); i++)
						server.clientThreads.get(i).printStream.println(line);
				}

				if (line.equals("GLOBAL_COMMIT") || line.equals("GLOBAL_ABORT")) {
					closed = true;
					break;
				}
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
