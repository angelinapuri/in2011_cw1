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
            // Start listening on given port
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening on " + ipAddress + ":" + portNumber);

            // Accept incoming connections in a separate thread
            Thread incomingConnectionsThread = new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Node connected!");
                        String nodeName = clientSocket.getInetAddress().getHostName();
                        String nodeAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                        handleIncomingConnections(nodeName, nodeAddress);
                    } catch (IOException e) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            });
            incomingConnectionsThread.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        try {
            // Split the startingNodeAddress into IP address and port number
            String[] parts = startingNodeAddress.split(":");
            String ipAddress = parts[0];
            int portNumber = Integer.parseInt(parts[1]);

            // Connect to the starting node
            socket = new Socket(ipAddress, portNumber);
            System.out.println("Connected to " + ipAddress + ":" + portNumber);
            System.out.println(notifyOtherFullNodes(ipAddress, portNumber));

            // Initialize reader and writer for socket communication
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new OutputStreamWriter(socket.getOutputStream());

            // Read incoming message from starting node
            String message = reader.readLine();
            String[] messageParts = message.split(" ");

       /**     if (messageParts.length > 0) {
                String requestType = messageParts[0];
                switch (requestType) {
                    case "START 1 " -> handleStartRequest();
                    case "NOTIFY?" -> handleNotifyRequest(ipAddress, startingNodeAddress);
                    case "ECHO" -> handleEchoRequest();
                    case "PUT?" -> handlePutRequest(messageParts);
                    case "GET?" -> handleGetRequest(messageParts);
                    default -> {
                        writer.write("Invalid request");
                        writer.flush();
                    }
                }
            } */

            // Close resources
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

 /**    private void handleStartRequest() throws IOException {
         writer.write("START 1 angelina.puri@city.ac.uk:test-01");
         writer.flush();
     }

     private void handleNotifyRequest(String startingNodeName, String startingNodeAddress) throws IOException {
         writer.write("NOTIFIED");
         writer.flush();
         NetworkMap.add(startingNodeName,startingNodeAddress);
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
                 writer.write("FAILED: Key not found");
             }
         } else {
             writer.write("Invalid message format");
         }
         writer.flush();
     }
*/
     private String notifyOtherFullNodes(String ipAddress, int portNumber) throws IOException {
            // Open a socket to communicate with other nodes
            Socket notifySocket = new Socket(ipAddress, portNumber);
            writer = new OutputStreamWriter(notifySocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(notifySocket.getInputStream()));

            // Prepare and send the NOTIFY? request
            String nodeName = "angelina.puri@city.ac.uk:test-01";
            writer.write("NOTIFY?" + "\n" + nodeName + "\n" + ipAddress + ":" + portNumber + "\n");
            System.out.println("NOTIFY?" + "\n" + nodeName + "\n" + ipAddress + ":" + portNumber + "\n");
            writer.flush();

            // Read and process the response from other nodes
            String response = reader.readLine();
            return response;

    }
}