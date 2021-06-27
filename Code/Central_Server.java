/*
Aaron Safer-Rosenthal
17asr
20068164
*/

import java.rmi.*;
import java.rmi.registry.*;

class Central_Server extends java.rmi.server.UnicastRemoteObject implements Central_Interface
{
	static String defaultString = "Central> ";

	// This is the constructor method for the Server class
	public Central_Server() throws RemoteException
	{
		System.out.println(defaultString + "Starting up ...");
	}

	// This messages returns the missing page number if there is a page fault
	public int swapMissingPage(int missedPageNumber) throws Exception
	{
		System.out.println(defaultString + "Page swapped in and send to requesting server");
		return missedPageNumber;
	}
	// This is main method for the Server
	public static void main (String args[])
	{
		int port = 12340; // Server communication port
		try {
			// Making, registering the server and binding it to a name
 			Central_Server S = new Central_Server();
			Registry R = LocateRegistry.createRegistry(port);
 			System.out.println(defaultString + "Ready ...");
 			R.rebind("CISC324", S);
		} catch(Exception e) {}
	}
}
