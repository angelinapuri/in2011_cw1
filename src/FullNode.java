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
                        handleNearestRequest(messageParts[1]);
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


        private void handleNearestRequest(String hashID) throws IOException {
            String nearestNodesResponse = networkMap.computeNearestNodes(hashID);
            writer.write(nearestNodesResponse);
            writer.flush();
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