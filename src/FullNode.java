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
            String nodeName = "angelina.puri@city.ac.uk:test-01";
            String nodeAddress = ipAddress + ":" + portNumber;
            NetworkMap.addNode(nodeName, nodeAddress);
            System.out.println("Added self as a node: " + nodeName + " at " + nodeAddress);

            while (true) {
                Socket acceptedSocket = serverSocket.accept();
                //System.out.println(sendNotifyRequest(nodeName, nodeAddress));
                System.out.println("New connection accepted");
                sendNotifyRequests(nodeName, nodeAddress);
                handleIncomingConnections(nodeName, nodeAddress);
                new Thread(new ClientHandler(acceptedSocket, networkMap)).start();
            }
        } catch (IOException e) {
            System.err.println("Exception listening for incoming connections");
            e.printStackTrace();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        System.out.println("Connected to " + startingNodeName + " at " + startingNodeAddress);
    }

    private void sendNotifyRequests(String startingNodeName, String startingNodeAddress) {
        List<NodeNameAndAddress> nodes = new ArrayList<>(networkMap.getMap().values());
     //   for (NodeNameAndAddress node : nodes) {
         //   String nodeName = node.getNodeName();
        //    String nodeAddress = node.getNodeAddress();
        String nodeName = "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000";
        String nodeAddress = "10.0.0.164:20000";
            sendNotifyRequest(nodeName, nodeAddress, startingNodeName, startingNodeAddress);
      //  }
    }

    private void sendStartMessage(String targetNodeName, String targetNodeAddress, String startingNodeName, String startingNodeAddress) {
        try {
            socket = new Socket(targetNodeAddress.split(":")[0], Integer.parseInt(targetNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.write("START 1 " + startingNodeName + "\n");
            System.out.println("START 1 " + startingNodeName + "\n");
            writer.flush();
            }
        catch (IOException e) {
            System.err.println("Error sending START message: " + e.getMessage());
        }
    }

    private void sendNotifyRequest(String targetNodeName, String targetNodeAddress, String startingNodeName, String startingNodeAddress) {
        try {
            
            // Send START message
            sendStartMessage( targetNodeName,  targetNodeAddress,  startingNodeName,  startingNodeAddress);

                writer.write("NOTIFY?" + "\n" + startingNodeName + "\n" + startingNodeAddress);
                writer.flush();

                System.out.println("Notify request sent to " + targetNodeName + " at " + targetNodeAddress);
                String response = reader.readLine();
                String response2 = reader.readLine();
                System.out.println(response);
                System.out.println(response2);

                writer.write("END: Notified Node");
                writer.flush();

            socket.close();

        } catch (IOException e) {
            System.err.println("Error sending notify request to " + targetNodeName + " at " + targetNodeAddress + ": " + e.getMessage());
        }
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
                    String request = messageParts[0];
                    if (request.equals("START")) {
                        if (!startMessageSent) {
                            handleStartRequest();
                            startMessageSent = true; // Set the flag to true after sending the START message
                        }
                    } else if (request.startsWith("NEAREST?")) {
                        handleNearestRequest(messageParts[1], networkMap);
                    } else if (request.equals("NOTIFY?")) {
                        handleNotifyRequest(reader);
                    } else if (request.equals("ECHO")) {
                        handleEchoRequest();
                    } else if (request.equals("PUT?")) {
                        handlePutRequest(reader);
                    } else if (request.equals("GET?")) {
                        handleGetRequest(reader, messageParts[1]);
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

        public void handleNearestRequest(String hashID, NetworkMap networkMap) {
            try {
                List<NodeNameAndAddress> nodes = new ArrayList<>(networkMap.getMap().values());

                Map<Integer, List<NodeNameAndAddress>> distances = new TreeMap<>();

                for (NodeNameAndAddress node : nodes) {
                    String nodeName = node.getNodeName();
                    String nodeAddress = node.getNodeAddress();
                    String nodeHashID = HashID.computeHashID(nodeName + "\n");
                    int distance = HashID.computeDistance(hashID, nodeHashID);

                    distances.putIfAbsent(distance, new ArrayList<>());
                    distances.get(distance).add(new NodeNameAndAddress(nodeName, nodeAddress));
                }

                StringBuilder responseBuilder = new StringBuilder();
                int count = 0;
                for (Map.Entry<Integer, List<NodeNameAndAddress>> entry : distances.entrySet()) {
                    List<NodeNameAndAddress> nearestNodes = entry.getValue();
                    for (NodeNameAndAddress node : nearestNodes) {
                        responseBuilder.append(node.getNodeName()).append("\n").append(node.getNodeAddress()).append("\n");
                        count++;
                        if (count >= 3) break;
                    }
                    if (count >= 3) break;
                }

                writer.write("NODES " + count + "\n" + responseBuilder.toString());
                writer.flush();

            } catch (Exception e) {
                System.err.println("Error handling NEAREST request: " + e.getMessage());
            }
        }

        private void handleNotifyRequest(BufferedReader reader) throws IOException {
            String message = reader.readLine();
            String[] messageLines = message.split("\n");
            StringBuilder messageBuilder = new StringBuilder();
            if (message.startsWith("NOTIFY?")) {
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

        private void handlePutRequest(BufferedReader reader) throws IOException {
            String keyValue = reader.readLine();
            String[] parts = keyValue.split(" ");

            int keyLineCount = Integer.parseInt(parts[1]);
            int valueLineCount = Integer.parseInt(parts[2]);

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

    private void handleGetRequest(BufferedReader reader, String keyLine) throws IOException {
        StringBuilder keyBuilder = new StringBuilder();
        int keyLines = Integer.parseInt(keyLine);

        // Append the provided key line to the keyBuilder
        keyBuilder.append(keyLine).append("\n");

        // Read the remaining key lines
        for (int i = 1; i < keyLines; i++) {
            String line = reader.readLine();
            if (line == null) {
                // Handle incomplete data
                writer.write("Incomplete data");
                writer.flush();
                return;
            }
            keyBuilder.append(line).append("\n");
        }

        String finalKey = keyBuilder.toString().trim();
        System.out.println("Final Key: " + finalKey);

        // Retrieve the value from the data store
        String value = dataStore.get(finalKey);

        // Send the response
        if (value != null && !value.isEmpty()) {
            int valueLines = value.split("\n").length;
            writer.write("VALUE " + valueLines + "\n" + value);
        } else {
            writer.write("NOPE");
        }
        writer.flush();
    }
}

/** For get method, make sure start lincha paila ani back and forth yeaa*/