/*
Aaron Safer-Rosenthal
17asr
20068164
*/

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;

public class Client1 {
	static int round = 1;
	static int n = 0;
	static String Stmp = null;
	static ObjectInputStream in;
	static ObjectOutputStream out;
	static ServerSocket ss;
	static Socket sc;
	static Socket ssc;
	static String[] strArray;
	static String defaultString = "Client 1> ";
	static boolean token = true; // Client 1 goes first!

	// This method sends a page to a sever, through a socket
	private static void sendPage(int myPort, int page, String IP, int serverPort) {
		try {
			sc = new Socket(IP, serverPort);
			System.out.println(defaultString + "Connected to " + getServerName(serverPort) + "server on port: " + serverPort);
			out = new ObjectOutputStream(sc.getOutputStream());
			Stmp = Integer.toString(myPort) + " " + Integer.toString(page);
			out.writeObject(Stmp);
			System.out.println(defaultString + "Requesting page number: " + page);
			out.flush();
			out.close();
			sc.close();
		} catch (Exception e) {}
	}

	// This method receives a page from a server, through a socket
	private static String[] receivePage(int myPort)  {
		try {
			System.out.println(defaultString + "Waiting for the page ...");
			ss = new ServerSocket(myPort);
			ssc = ss.accept();
			in = new ObjectInputStream(ssc.getInputStream());
			Stmp = (String) in.readObject();
			strArray = Stmp.split(" ");
			int senderPort = Integer.parseInt(strArray[0]);
			n = Integer.parseInt(strArray[1]);
			in.close();
			ss.close();
		} catch (Exception e) {}
		return strArray;
	}

	// This method sends a the token to the other client
	private static void sendToken(int myPort, String IP, int serverPort) {
		try {
			sc = new Socket(IP, serverPort);
			out = new ObjectOutputStream(sc.getOutputStream());
			System.out.println(defaultString + "Connected to client 2 on port: 12345");
			System.out.println(defaultString + "Sending token to client 2");
			out.writeObject(true);
			out.flush();
			out.close();
			sc.close();
		} catch (Exception e) {}
	}

	// This method receives the token from the other client
	private static boolean receiveToken(int myPort)  {
		try {
			ss = new ServerSocket(myPort);
			ssc = ss.accept();
			System.out.println(defaultString + "Connection established with client 2.");
			in = new ObjectInputStream(ssc.getInputStream());
			token = (boolean) in.readObject();
			System.out.println(defaultString + "Received token from client 2.");
			in.close();
			ss.close();
		} catch (Exception e) {}
		return token;
	}

	// Returns the server name, based on its port number
	public static String getServerName(int portNumber)
	{
			String serverName = "";
			if (portNumber == 12347) {
				serverName = "FIFO";
			}
			else if (portNumber == 12348) {
				serverName = "LFU";
			}
			else if (portNumber == 12349) {
				serverName = "MFU";
			}
			return serverName;
	}

	public static void main(String[] args)
	{
		String IP = "127.0.0.1";
		String[] receivedPage;
		int receiveFromPort;
		int myPort = 12346;
		int[] serverPorts = {12347, 12348, 12349}; // Ports are FIFOServer, LFUServer, MFUServer and Client 2, respectively
		int[] referenceString = {7, 0, 1, 2, 4, 3, 0, 6, 2, 3, 0, 5, 2, 1, 0, 5, 1, 7, 0, 2};

		// Running for each element in the page array
		for (Integer reference : referenceString) {
			try {
				while (!token) {
					token = receiveToken(myPort);
				}
				for (Integer serverPort : serverPorts) {
					sendPage(myPort, reference, IP, serverPort);
					receivedPage = receivePage(myPort);
					receiveFromPort = Integer.parseInt(receivedPage[0]);
					serverPort = receiveFromPort;
					n = Integer.parseInt(receivedPage[1]);
					System.out.println(defaultString + "Page " + n + " received from " + getServerName(serverPort) + " server");
				}
				token = false;
				sendToken(myPort, IP, 12345);
				Thread.sleep(500);
				System.out.println(defaultString + "Waiting for connection from client 2 ...");
			} catch(Exception e) {}
		}
		for (Integer serverPort : serverPorts) {
			sendPage(myPort, -1, IP, serverPort); // Sending 'terminating page'
		}
	}
}
