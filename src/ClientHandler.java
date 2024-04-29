import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean startMessageSent = false; // Flag to track if the START message has been sent
    private static NetworkMap networkMap;
    private DataStore dataStore;

    public ClientHandler(Socket clientSocket, NetworkMap networkMap, DataStore dataStore) {
        this.clientSocket = clientSocket;
        ClientHandler.networkMap = networkMap;
        this.dataStore = dataStore;

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
            String nodeName = "angelina.puri@city.ac.uk:test-01";
            String nodeAddress = "10.0.0.119:20000";
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
                        handleStartRequest(nodeName);
                        startMessageSent = true; // Set the flag to true after sending the START message
                    }
                } else if (request.startsWith("NEAREST?")) {
                    handleNearestRequest(messageParts[1], networkMap);
                } else if (request.equals("NOTIFY?")) {
                    handleNotifyRequest(reader);
                } else if (request.equals("ECHO")) {
                    handleEchoRequest();
                } else if (request.equals("PUT?")) {
                    handlePutRequest(reader, messageParts[1], messageParts[2], nodeName, nodeAddress);
                } else if (request.equals("GET?")) {
                    handleGetRequest(reader, messageParts[1]);
                } else if (request.startsWith("END")) {
                    handleEndRequest();
                }else {
                    writer.write("Unknown command");
                    writer.flush();
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void handleStartRequest(String nodeName) throws IOException {
        writer.write("START 1 " + nodeName + "\n");
        writer.flush();
    }

    public void handleNearestRequest(String hashID, NetworkMap networkMap) throws IOException {
        String nearestNodes = NetworkMap.getNearestNodes(hashID);
        assert nearestNodes != null;
        writer.write(nearestNodes);
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

    private void handlePutRequest(BufferedReader reader, String keyLine, String valueLine, String nodeName, String nodeAddress) throws Exception {

        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        int keyLines = Integer.parseInt(keyLine);
        for (int i = 0; i < keyLines; i++) {
            String line = reader.readLine();
            keyBuilder.append(line).append("\n");
        }

        int valueLines = Integer.parseInt(valueLine);
        for (int i = 0; i < valueLines; i++) {
            String line = reader.readLine();
            valueBuilder.append(line).append("\n");
        }

        String key = keyBuilder.toString().trim();
        String value = valueBuilder.toString().trim();
        System.out.println(key);
        System.out.println(value);

        String keyHash = HashID.computeHashID(key + "\n");

        String nearestNodes = NetworkMap.getNearestNodes(keyHash);
        assert nearestNodes != null;
        String[] nearestNodesLines = nearestNodes.split("\n");

        boolean nodeFound = false;
        for (int i = 1; i < nearestNodesLines.length-1; i += 2) {
            String nearestNodeName = nearestNodesLines[i];
            String nearestNodeAddress = nearestNodesLines[i + 1];

            if (nearestNodeName.equals(nodeName) && nearestNodeAddress.equals(nodeAddress)) {
                dataStore.store(key, value);
                writer.write("SUCCESS" + "\n");
                writer.flush();
                dataStore.printContents();
                nodeFound = true;
                break;
            }
        }

        if (!nodeFound) {
            writer.write("FAILED" + "\n");
            writer.flush();
        }
    }

    private void handleGetRequest(BufferedReader reader, String keyLine) throws IOException {

        StringBuilder keyBuilder = new StringBuilder();

        int keyLines = Integer.parseInt(keyLine);
            for (int i = 0; i < keyLines; i++) {
                String line = reader.readLine();
                keyBuilder.append(line).append("\n");
            }

        String key = keyBuilder.toString().trim();
        System.out.println(key);

        String value = dataStore.get(key);
        System.out.println(value);

        if (value == null) {
            writer.write("NOPE" + "\n");
            writer.flush();
        } else {
            int valueLines = value.split("\n").length;
            writer.write("VALUE " + valueLines + "\n" + value + "\n");
            writer.flush();
        }
    }

    private void handleEndRequest() throws IOException {
        clientSocket.close();
    }
}