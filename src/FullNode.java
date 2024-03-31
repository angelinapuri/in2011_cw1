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
 import java.util.*;

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
             String nodeName = "angelina.puri@city.ac.uk";
             String nodeAddress = ipAddress + ":" + portNumber;
             NetworkMap.addNode(nodeName, nodeAddress);
             //System.out.println("Added self as a node: " + "angelina.puri@city.ac.uk" + " at " + nodeAddress);

             while (true) {
                 Socket acceptedSocket = serverSocket.accept();
                // System.out.println(sendNotifyRequest(ipAddress, portNumber, acceptedSocket));
                 System.out.println("New connection accepted");
                 new Thread(new ClientHandler(acceptedSocket, networkMap)).start();
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
         private NetworkMap networkMap;

         public ClientHandler(Socket clientSocket, NetworkMap networkMap) {
             this.clientSocket = clientSocket;
             this.networkMap = networkMap;
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
                 String message=reader.readLine();
                 System.out.println(message);
                 while(message != null) {
                     if (message.startsWith("START")) {
                         handleStartRequest();
                     } else if (message.startsWith("NEAREST?")) {
                         handleNearestRequest(message, networkMap);
                     } else if (message.startsWith("NOTIFY?")) {
                         handleNotifyRequest(message);
                     } else if (message.equals("ECHO")) {
                         handleEchoRequest();
                     } else if (message.startsWith("PUT?")) {
                         handlePutRequest(message);
                     } else if (message.startsWith("GET?")) {
                         handleGetRequest(message);
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


         private void handleNearestRequest(String message, NetworkMap networkMap) throws IOException {
             try {
                 String[] messageParts = message.split(" ");
                 String hashID = messageParts[1];
                 Map<Integer, List<Node>> distances = new TreeMap<>();

                 // Compute distances to all nodes in the map
                 for (Map.Entry<String, String> entry : NetworkMap.getMap().entrySet()) {
                     String nodeName = entry.getKey();
                     String nodeAddress = entry.getValue();
                     String nodeHashID = HashID.computeHashID(nodeName + "\n");
                     int distance = HashID.computeDistance(hashID, nodeHashID);

                     distances.putIfAbsent(distance, new ArrayList<>());
                     distances.get(distance).add(new Node(nodeName, nodeAddress));
                 }

                 List<Node> closestNodes = new ArrayList<>();
                 int count = 0;

                 // Iterate through distances and add closest nodes to the list
                 for (Map.Entry<Integer, List<Node>> entry : distances.entrySet()) {
                     List<Node> closestNodesAtDistance = entry.getValue();
                     Collections.shuffle(closestNodesAtDistance); // Shuffle to randomize selection

                     for (Node closestNode : closestNodesAtDistance) {
                         closestNodes.add(closestNode);
                         count++;

                         if (count >= 3) {
                             break; // Exit the loop if maximum count reached
                         }
                     }

                     if (count >= 1) {
                         break; // Exit the loop if at least one node added
                     }
                 }
                 StringBuilder nodeList = new StringBuilder();
                 for (Node node : closestNodes) {
                     nodeList.append(node.getName()).append("\n").append(node.getAddress()).append("\n");
                 }
                 writer.write("NODES " + count + "\n" + nodeList.toString()); //might not need toString
                 //System.out.println("NODES " + count + "\n" + nodeList.toString());
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

         private void handlePutRequest(String message) throws IOException {
                 String keyLines = null;
                 String valueLines = null;

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
             }
         }

         private void handleGetRequest(String message) throws IOException {
             String[] messageParts = message.split(" ");

             if (messageParts.length != 2) {
                 writer.write("Invalid message format");
                 writer.flush();
                 return;
             }

             // Extract the key lines count from the message
             int keyLineCount = Integer.parseInt(messageParts[1]);

             StringBuilder keyBuilder = new StringBuilder();

             // Read each key from the message
             for (int i = 0; i < keyLineCount; i++) {
                 String line = reader.readLine();
                 if (line == null) {
                     writer.write("FAILED: Incomplete key");
                     writer.flush();
                     return;
                 }
                 keyBuilder.append(line).append("\n");
             }

             String finalKey = keyBuilder.toString().trim();
             String value = dataStore.get(finalKey);

             if (value != null) {
                 writer.write("VALUE " + value.length() + "\n" + value + "\n");
             } else {
                 writer.write("NOPE");
             }

             writer.flush();
         }

     }
     /** For get method, make sure start lincha paila ani back and forth yeaa*/