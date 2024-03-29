// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

// DO NOT EDIT starts
interface FullNodeInterface {
 public boolean listen(String ipAddress, int portNumber);
 public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
 private ServerSocket serverSocket;
 private Socket socket;
 private Writer writer;
 private BufferedReader reader;
 private NetworkMap networkMap;
 private DataStore dataStore;

 public FullNode(NetworkMap networkMap, DataStore dataStore) throws IOException {
  this.networkMap = networkMap;
  this.dataStore = dataStore;
 }


 public FullNode() {
  networkMap = new NetworkMap();
  dataStore = new DataStore();

 }

 public boolean listen(String ipAddress, int portNumber) {
  try {
     System.out.println("Listening on port:" + portNumber);
     ServerSocket serverSocket = new ServerSocket(portNumber);

     System.out.println("Server waiting for client...");
     Socket clientSocket = serverSocket.accept();
     System.out.println("Node connected!");

   return true;
  } catch (IOException e) {
   e.printStackTrace();
   return false;
  }
 }

 public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
  try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       Writer writer = new OutputStreamWriter(socket.getOutputStream())) {

   // Read a line from the client
   String message = reader.readLine();
   System.out.println("The client said : " + message);

   // Send a message to the client
   System.out.println("Sending a message to the client");
   writer.write("Nice to meet you\n");
   writer.flush();

  } catch (Exception e) {
   throw new RuntimeException(e);
  }
 }
}