// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.List;
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

     public FullNode(NetworkMap networkMap, DataStore dataStore) {
         this.networkMap = networkMap;
         this.dataStore = dataStore;
     }


     public FullNode() {
         networkMap = new NetworkMap();
         dataStore = new DataStore();

     }

     public boolean listen(String ipAddress, int portNumber) {
         try {
             serverSocket = new ServerSocket(portNumber);
             System.out.println("Listening for incoming connections on " + ipAddress + ":" + portNumber);
             String nodeAddress = ipAddress + portNumber;
             NetworkMap.addNode("angelina.puri@city.ac.uk", nodeAddress);
             System.out.println("Added self as a node: " + "angelina.puri@city.ac.uk" + " at " + nodeAddress);

             while (true) {
                 Socket acceptedSocket = serverSocket.accept();
                // System.out.println(sendNotifyRequest(ipAddress, portNumber, acceptedSocket));
                 System.out.println("New connection accepted");
                 new Thread(new ClientHandler(acceptedSocket)).start();
             }
         } catch (IOException e) {
             System.err.println("Exception listening for incoming connections");
             e.printStackTrace();
             return false;
         }
     }

     public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
         NetworkMap.addNode(startingNodeName, startingNodeAddress);
         System.out.println("Connected to " + startingNodeName + " at " + startingNodeAddress);
     }

     private class ClientHandler implements Runnable {
         private Socket clientSocket;
         private BufferedWriter writer;
         private BufferedReader reader;
         private boolean startMessageSent = false; // Flag to track if the START message has been sent

         public ClientHandler(Socket clientSocket) {
             this.clientSocket = clientSocket;
             try {
                 this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                 this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             } catch (IOException e) {
                 System.err.println("Error initializing client socket streams: " + e.getMessage());
                 e.printStackTrace();
             }
         }

         @Override
         public void run() {
             try {
                 String message;
                 while ((message = reader.readLine()) != null) {
                     System.out.println(message);
                     String[] messageParts = message.split(" ");
                     if (messageParts.length == 0) {
                         // Handle empty message or unexpected format
                         writer.write("Invalid message format");
                         writer.flush();
                         continue; // Skip further processing
                     }
                     String operation = messageParts[0];
                     if (operation.equals("START")) {
                         if (!startMessageSent) {
                             handleStartRequest();
                             startMessageSent = true; // Set the flag to true after sending the START message
                         }
                         else if (operation.equals("NEAREST?")) {
                             handleNearestRequest(messageParts[1]);
                         }
                     } else if (operation.equals("NOTIFY?")) {
                         handleNotifyRequest(message);
                     } else if (operation.equals("ECHO")) {
                         handleEchoRequest();
                     } else if (operation.equals("PUT?")) {
                         handlePutRequest(messageParts);
                     } else if (operation.equals("GET?")) {
                         handleGetRequest(messageParts);
                     } else {
                         writer.write("Unknown command");
                         writer.flush();
                     }
                 }
             } catch (IOException e) {
                 System.out.println("Error: " + e.getMessage());
             } finally {
                 try {
                     clientSocket.close();
                 } catch (IOException e) {
                     System.out.println("Error closing client socket: " + e.getMessage());
                 }
             }
         }



         private void handleStartRequest() throws IOException {
             writer.write("START 1 angelina.puri@city.ac.uk:test-01" + "\n");
             writer.flush();
         }

         private void handleNearestRequest(String hashID) throws IOException {
             try {
                 List<Node> closestNodes = NetworkMap.findClosestNodes(hashID);
                 System.out.println(closestNodes);
                 writer.write(closestNodes.toString());
                 writer.flush();

             } catch (Exception e) {
                 System.err.println("Error handling NEAREST request: " + e.getMessage());
                 writer.write("ERROR\n");
                 writer.flush();
             }
         }



         private void handleNotifyRequest(String message) throws IOException {

             String[] messageLines = message.split("\n");
             StringBuilder messageBuilder = new StringBuilder();
             if (message.startsWith("NOTTIFY?")) {
                 messageBuilder.append(message).append("\n");
                 String startingNodeName = null;
                 for (int i = 1; i < messageLines.length; i++) {
                     String line = reader.readLine();
                     messageBuilder.append(line).append("\n");
                     startingNodeName = messageBuilder.toString().trim();
                 }
                 String startingNodeAddress = null;
                 for (int i = 2; i < messageLines.length; i++) {
                     String line = reader.readLine();
                     messageBuilder.append(line).append("\n");
                     startingNodeAddress = messageBuilder.toString().trim();
                 }
                 NetworkMap.addNode(startingNodeName, startingNodeAddress);
                 writer.write("NOTIFIED" + "\n");
                 writer.flush();
             }
         }

         private void handleEchoRequest() throws IOException {
             writer.write("OHCE" + "\n");
             writer.flush();
         }

         private void handlePutRequest(String[] messageParts) throws IOException {
             if (messageParts.length == 3) {
                 String keyLines = messageParts[1];
                 String valueLines = messageParts[2];

                 int keyLineCount = Integer.parseInt(keyLines);
                 int valueLineCount = Integer.parseInt(valueLines);

                 StringBuilder keyBuilder = new StringBuilder();
                 StringBuilder valueBuilder = new StringBuilder();

                 // Read the key lines
                 for (int i = 0; i < keyLineCount; i++) {
                     String line = reader.readLine();
                     if (line == null) {
                         writer.write("FAILED: Incomplete key");
                         writer.flush();
                         return;
                     }
                     keyBuilder.append(line).append("\n");
                 }

                 // Read the value lines
                 for (int i = 0; i < valueLineCount; i++) {
                     String line = reader.readLine();
                     if (line == null) {
                         writer.write("FAILED: Incomplete value");
                         writer.flush();
                         return;
                     }
                     valueBuilder.append(line).append("\n");
                 }

                 // Store the key-value pair
                 String key = keyBuilder.toString().trim();
                 String value = valueBuilder.toString().trim();
                 dataStore.store(key, value);
                 writer.write("SUCCESS");
                 writer.flush();
             } else {
                 writer.write("Invalid request");
                 writer.flush();
             }
         }

         private void handleGetRequest(String[] messageParts) throws IOException {
             if (messageParts.length == 2) {
                 String keyLines = messageParts[1];
                 int keyLineCount = Integer.parseInt(keyLines);

                 StringBuilder keyBuilder = new StringBuilder();

                 // Read the key lines
                 for (int i = 0; i < keyLineCount; i++) {
                     String line = reader.readLine();
                     if (line == null) {
                         writer.write("FAILED: Incomplete key");
                         writer.flush();
                         return;
                     }
                     keyBuilder.append(line).append("\n");
                 }
                 String key = keyBuilder.toString().trim();
                 String value = dataStore.get(key);

                 if (value != null) {
                     writer.write("VALUE " + value.length() + "\n");
                     writer.write(value);
                 } else {
                     writer.write("NOPE");
                 }
             } else {
                 writer.write("Invalid message format");
             }
             writer.flush();
         }

     }
 }
     /** For get method, make sure start lincha paila ani back and forth yeaa*/