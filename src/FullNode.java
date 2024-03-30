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
             ServerSocket serverSocket = new ServerSocket(portNumber);
             System.out.println("Listening for incoming connections on " + ipAddress + ":" + portNumber);
             while (true) {
                 Socket socket = serverSocket.accept();
                 System.out.println("New connection accepted");
                 new Thread(new ClientHandler(socket)).start();
             }
         } catch (IOException e) {
             System.err.println("Exception listening for incoming connections");
             System.err.println(e);
             return false;
         }
     }

     public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
         networkMap.addNode(startingNodeName, startingNodeAddress);
         System.out.println("Connected to " + startingNodeName + " at " + startingNodeAddress);
         try {
             writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

             String command;
             while ((command = reader.readLine()) != null) {
                 String[] messageParts = command.split(" ");
                 String operation = messageParts[0];
                 switch (operation) {
                     case "START":
                         if (messageParts.length > 1) { 
                             handleStartRequest(messageParts[1]);}
                         break;
                     case "notify":
                         handleNotifyRequest(startingNodeName, startingNodeAddress);
                         break;
                     case "echo":
                         handleEchoRequest();
                         break;
                     case "put":
                         handlePutRequest(messageParts);
                         break;
                     case "get":
                         handleGetRequest(messageParts);
                         break;
                     default:
                         System.out.println("Unknown command");
                 }
             }
         } catch (IOException e) {
             System.err.println("Exception handling incoming connections");
             e.printStackTrace();
         }
     }

     private void handleStartRequest(String startingNodeAddress) {
         try {
             writer.write("START 1 angelina.puri@city.ac.uk:test-01");
             writer.flush();
             socket.close();
         } catch (IOException e) {
             System.err.println("Error handling START request: " + e.getMessage());
         }
     }

     private void handleNotifyRequest(String startingNodeName, String startingNodeAddress) {
         try {
             networkMap.addNode(startingNodeName, startingNodeAddress);
             Socket socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
             writer.write("NOTIFIED");
             writer.flush();
             socket.close();
         } catch (IOException e) {
             System.err.println("Error handling NOTIFY request: " + e.getMessage());
         }
     }

     private void handleEchoRequest() throws IOException {
         writer.write("OHCE");
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

         private class ClientHandler implements Runnable {
             private Socket clientSocket;
             private BufferedReader reader;
             private BufferedWriter writer;

             public ClientHandler(Socket clientSocket) {
                 this.clientSocket = clientSocket;
             }

             @Override
             public void run() {
                 try {
                     reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                     String message;
                     while ((message = reader.readLine()) != null) {
                         System.out.println("Received: " + message);
                         String[] messageParts = message.split(" ");

                         switch (messageParts[0]) {
                             case "START":
                                 handleStartRequest(messageParts[1]);
                                 break;
                             case "NOTIFY?":
                                 handleNotifyRequest(messageParts[1], messageParts[2]);
                                 break;
                             case "ECHO":
                                 handleEchoRequest();
                                 break;
                             case "PUT":
                                 handlePutRequest(messageParts);
                                 break;
                             case "GET":
                                 handleGetRequest(messageParts);
                                 break;
                             default:
                                 writer.write("Invalid request");
                                 writer.flush();
                                 break;
                         }
                     }
                 } catch (IOException e) {
                     System.out.println("Error: " + e.getMessage());
                 } finally {
                     try {
                         clientSocket.close();
                     } catch (IOException e) {
                         System.out.println("Error: " + e.getMessage());
                     }
                 }
             }
         }
     }