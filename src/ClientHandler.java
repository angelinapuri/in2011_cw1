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
    private NetworkMap networkMap;
    private DataStore dataStore;

    public ClientHandler(Socket clientSocket, NetworkMap networkMap, DataStore dataStore) {
        this.clientSocket = clientSocket;
        this.networkMap = networkMap;
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

    private void handleGetRequest(BufferedReader reader, String keyLine) throws IOException {

        StringBuilder keyBuilder = new StringBuilder();

        int keyLines = parseInt(keyLine);
            for (int i = 0; i < keyLines; i++) {
                String line = reader.readLine();
                keyBuilder.append(line).append("\n");
            }

        String finalKey = keyBuilder.toString().trim();
        System.out.println("Final Key: " + finalKey);

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