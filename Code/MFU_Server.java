/*
Aaron Safer-Rosenthal
17asr
20068164
*/

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.*;
//import java.util.Iterator;
import java.util.*;

public class MFU_Server
  {
    static int pageNum = 0;
    static String Stmp = null;
    static ObjectInputStream in;
    static ObjectOutputStream out;
    static ServerSocket ss;
    static Socket sc;
    static Socket ssc;
    static String[] strArray;
    static Central_Interface S;
    static Registry R;
    static int HDDResult;
    static int client1PageFaultCounter = 0;
    static int client2PageFaultCounter = 0;
    static int client1PageCounter = 0;
    static int client2PageCounter = 0;
    static int receiveFromPort;
    static int myPort = 12349;
    static int socketServerPort;
    static List<Integer> currentRAM  = new ArrayList<>();
    static Queue<Integer> frameQueue = new LinkedList<>();
    static String defaultString = "Server[MFU]> ";
    static int frameSize = 3; // Change for testing
    static int[][] currentRAMFreq = setCurrentRAMFreq();

    // constructor for the currentRAMFreq variable
    static int[][] setCurrentRAMFreq() {
      currentRAMFreq = new int[frameSize][2];
      for (int i = 0; i < frameSize; i++) {
        currentRAMFreq[i][0] = -1;
        //currentRAMFreq[i][1] = Integer.MAX_VALUE;
      }
      return currentRAMFreq;
    }

  // This method sends content to a sever, through a socket
  private static void sendPage(int myPort, int content, String IP, int serverPort) {
    try {
      sc = new Socket(IP, serverPort);
      out = new ObjectOutputStream(sc.getOutputStream());
      Stmp = Integer.toString(myPort)+" "+Integer.toString(content);
      out.writeObject(Stmp);
      out.flush();
      out.close();
      sc.close();
    } catch (Exception e) {}
  }

  // This method receives content from a server, or the other client, through a socket
  private static String[] receivePage(int myPort) {
    try {
      ss = new ServerSocket(myPort);
      ssc = ss.accept();
      //receiving content
      in = new ObjectInputStream(ssc.getInputStream());
      Stmp = (String) in.readObject();
      strArray = Stmp.split(" ");
      int receiveFromPort = Integer.parseInt(strArray[0]);
      pageNum = Integer.parseInt(strArray[1]);
      String clientNum;
      if (receiveFromPort == 12346) { // Client 1
        clientNum = "1";
        client1PageCounter++;
      }
      else { //(else if receiveFromPort == 12345) // Client 2
        clientNum = "2";
        client2PageCounter++;
      }
      System.out.println(defaultString + "Waiting for connection from client " + clientNum);
      System.out.println(defaultString + "Connection established from client " + clientNum);
      System.out.println(defaultString + "received request from client " + clientNum);
      in.close();
      ss.close();
    } catch (Exception e) {}
    return strArray;
  }

  //Checks to see whether there is a page fault or not and deals with one, if it is caused
  static int faultCheck(int pageNum, int receiveFromPort) {
    RAMFormat(currentRAM);// Printing current RAM
    System.out.println(defaultString + "Sending response ...");
    // Starting by assuming a page fault
    boolean pageFault = true;
    // If the page is in the frame (hit/no page fault)
    if (currentRAM.contains(pageNum)) {
      // Adding 1 to its frequency
      for (int[] pageFreq : currentRAMFreq) {
        if (pageFreq[0] == pageNum) {
          pageFreq[1]++;
        }
      }
      return pageNum;
    }
    // If there is a page fault
    if (pageFault == true)
    {
      // Adding to a client's total fault counter, depending on which client sent the fault
      int clientNum = (receiveFromPort == 12346) ? 1 : 2;
      // Client 1
      if(clientNum == 1) {
        client1PageFaultCounter++;
      }
      // Client 2
      else if(clientNum == 2) {
        client2PageFaultCounter++;
      }
      try {
        HDDResult = S.swapMissingPage(pageNum);
      } catch (Exception e) {}
      if (currentRAM.size() < frameSize) { //page fault but page elements number below max frame size
        boolean updated = false;
        for (int[] pageFreq : currentRAMFreq) {
          if (pageFreq[0] == -1 && !updated) {
            pageFreq[0] = pageNum;
            pageFreq[1] = 1;
            updated = true;
          }
        }
        currentRAM.add(HDDResult);
      }
      // There is a page fault and the page elements reach the max frame size
      else { // There is a page fault and frame is full
        int MFUPage = getMFU();
        // Updating current RAM
        currentRAM.remove(Integer.valueOf(MFUPage));
        currentRAM.add(HDDResult);
        // Updating current RAM pageFrequencies
        for (int[] pageFreq : currentRAMFreq) {
          if (pageFreq[0] == MFUPage) {
            pageFreq[0] = HDDResult;
            pageFreq[1] = 1;
          }
        }
      }
    }
    RAMFormat(currentRAM); // Printing current RAM status
    return HDDResult;
  }

  // Applies MFU algorithm to get the least frequently used page
  static int getMFU()
  {
    int mostFreq = Integer.MIN_VALUE;
    int MFUPage = -1; // Placeholder value
    // Finding MFU Page
    for (int[] pageFreq : currentRAMFreq) {
      if (pageFreq[1] > mostFreq) {
        MFUPage = pageFreq[0];
        mostFreq = pageFreq[1];
      }
    }
    return MFUPage;
  }

  static void RAMFormat(List<Integer> currentRAM) {
    String RAMString = defaultString + "Current RAM: " + "[";
    Iterator<Integer> iterator = currentRAM.iterator();
    while (iterator.hasNext()) {
        RAMString += iterator.next().toString();
        if (iterator.hasNext()) {
            RAMString += "|";
        }
        else {
          RAMString += "]";
          System.out.println(RAMString);
        }
     }
  }

  // Prints results and exits the program
  // client1PageCounter-1 since the last page is a 'terminating page'
  static void printResults() {
    System.out.println(defaultString + "on " + (client1PageCounter - 1) + " references from client 1, got " + client1PageFaultCounter + " page faults");
    System.out.println(defaultString + "on " + client2PageCounter + " references from client 2, got " + client2PageFaultCounter + " page faults");
    System.out.println(defaultString + "on " + ((client1PageCounter - 1) + client2PageCounter) + " references from client 2, got " + (client1PageFaultCounter + client2PageFaultCounter) + " page faults");
    System.exit(0);
  }

  public static void main(String[] args)
  {
    int pageNum = 0;
    String[] receivedContent;
    System.out.println(defaultString + "Waiting for connection from client 1 ...");
    while (pageNum != -1) {
      try {
        // Locate a server which IP address is 127.0.0.1 (localhost) and which is listening on port 12340
        R = LocateRegistry.getRegistry("127.0.0.1", 12340);
        // Using the server name CISC324, obtain a reference of the remote object (server)
        S = (Central_Interface) (R.lookup("CISC324"));
        receivedContent = receivePage(myPort);
        pageNum = Integer.parseInt(receivedContent[1]);
        if (pageNum == -1) { // Done!
          printResults();
        }
        receiveFromPort = Integer.parseInt(receivedContent[0]);
        socketServerPort = receiveFromPort;
        pageNum = faultCheck(pageNum, receiveFromPort);
        Thread.sleep(500);
        sendPage(myPort, pageNum, "127.0.0.1", socketServerPort);	// Send result back to client
        } catch (Exception e) {}
      }
  }
}
