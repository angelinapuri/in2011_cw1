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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private static NetworkMap networkMap;
    private final DataStore dataStore;
    private final String nodeName;
    private final String ipAddress;
    private final int portNumber;
    private String requesterNodeName;

    public ClientHandler(Socket clientSocket, NetworkMap networkMap, DataStore dataStore, String nodeName, String ipAddress,int portNumber) {
        this.clientSocket = clientSocket;
        ClientHandler.networkMap = networkMap;
        this.dataStore = DataStore.getInstance();
        this.nodeName = nodeName;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;

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
            String nodeAddress = ipAddress + ":" + portNumber;
            String requesterNodeAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();

            //Send a START message to client
            handleStartRequest(nodeName, requesterNodeAddress);

            //Handle multiple requests from client
            while (true) {

                long startTime = System.currentTimeMillis();
                long timeoutMillis = 60000;

                String message = reader.readLine();
                System.out.println(message);
                String[] messageParts = message.split(" ");
                if (messageParts.length == 0) {
                    writer.write("Invalid message format");
                    writer.flush();
                    continue;
                }
                String request = messageParts[0];
                if (request.equals("ECHO?")) {
                    handleEchoRequest();
                }else if (request.equals("NOTIFY?")) {
                    handleNotifyRequest(reader, requesterNodeName, requesterNodeAddress);
                }else if (request.startsWith("NEAREST?")) {
                    handleNearestRequest(messageParts[1], networkMap, requesterNodeName, requesterNodeAddress);
                } else if (request.equals("PUT?")) {
                    handlePutRequest(reader, messageParts[1], messageParts[2], nodeName, nodeAddress);
                } else if (request.equals("GET?")) {
                    handleGetRequest(reader, messageParts[1]);
                } else if (request.startsWith("END")) {
                    handleEndRequest(requesterNodeName, requesterNodeAddress);
                } else if((System.currentTimeMillis() - startTime) > timeoutMillis){
                    writer.write("No new messages received from: " + requesterNodeAddress);
                    writer.flush();
                    //Remove client node to the map
                    NetworkMap.removeNode(requesterNodeName, requesterNodeAddress);
                    break;
                } else {
                    writer.write("END: Unknown command");
                    writer.flush();
                    //Remove client node to the map
                    NetworkMap.removeNode(requesterNodeName, requesterNodeAddress);
                    break;
                }
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout error: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    //Handle START request from client
    private void handleStartRequest(String nodeName, String requesterNodeAddress) {
        try {
            writer.write("START 1 " + nodeName + "\n");
            writer.flush();
            String startMessage = reader.readLine();

            System.out.println(startMessage);
            String[] startMessageParts = startMessage.split(" ");

            String requesterNodeName = startMessageParts[2];
            this.requesterNodeName = requesterNodeName;


            if(!startMessageParts[0].equals("START")) {
                throw new Exception("Start messages must have three parts");
            }
            if(startMessageParts.length!=3) {
                throw new Exception("Start messages must have three parts");
            }
            else if(!startMessageParts[1].equals("1")) {
                throw new Exception("Incorrect protocol number! (It should be 1)");
            }
            else if(!requesterNodeName.contains("@") && !startMessageParts[2].contains(".")) {
                throw new Exception("Node names must contain a valid e-mail address");
            }
            else if(!requesterNodeName.contains(":")) {
                throw new Exception("Node names must contain a colon");
            }
            //Add client node to the map
            NetworkMap.addNode(requesterNodeName, requesterNodeAddress);
        }
        catch(Exception e) {
            try {
                writer.write("END java.lang.Exception: " + e.getMessage() + "\n");
                writer.flush();
                clientSocket.close();
            } catch (IOException ioException) {
                System.err.println("Error handling client connection: " + ioException.getMessage());
            }
        }
    }

    //Handle NEAREST? request from client
    public void handleNearestRequest(String hashID, NetworkMap networkMap, String requesterNodeName, String requesterNodeAddress)  {
        try {
//            if(!hashID.matches("^[0-9a-zA-Z]{64}$")) {
//                throw new Exception("A hashID must have 64 hex digits.");
//            }
            String nearestNodes = NetworkMap.getNearestNodes(hashID);
            assert nearestNodes != null;
            writer.write(nearestNodes);
            writer.flush();
        }
        catch(Exception e) {
            try {
                writer.write("END java.lang.Exception: " + e.getMessage() + "\n");
                writer.flush();
                //Remove client node to the map
                NetworkMap.removeNode(requesterNodeName, requesterNodeAddress);
                clientSocket.close();
            } catch (IOException ioException) {
                System.err.println("Error handling client connection: " + ioException.getMessage());
            }
        }
    }

    //Handle NOTIFY? request from client
    private void handleNotifyRequest(BufferedReader reader, String requesterNodeName, String requesterNodeAddress) {
        try{
            String notifierNodeName= reader.readLine();
            if(!notifierNodeName.contains("@") && !notifierNodeName.contains(".")) {
                throw new Exception("Node names must contain a valid e-mail address");
            }
            else if(!notifierNodeName.contains(":")) {
                throw new Exception("Node names must contain a colon");
            }

            String notifierNodeAddress= reader.readLine();
            if(!notifierNodeAddress.contains(":")) {
                throw new Exception("Address must contain :");
            }
            //Add client node to the map
            NetworkMap.addNode(notifierNodeName, notifierNodeAddress);

            writer.write("NOTIFIED" + "\n");
            writer.flush();
        }

        catch(Exception e) {
            try {
                writer.write("END java.lang.Exception: " + e.getMessage() + "\n");
                writer.flush();
                //Remove client node to the map
                NetworkMap.removeNode(requesterNodeName, requesterNodeAddress);
                clientSocket.close();
            } catch (IOException ioException) {
                System.err.println("Error handling client connection: " + ioException.getMessage());
            }
        }
    }

    //Handle ECHO? request from client
    private void handleEchoRequest() throws IOException {
        writer.write("OHCE" + "\n");
        writer.flush();
    }

    //Handle PUT? request from client
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
                //dataStore.printContents();
                nodeFound = true;
                break;
            }
        }

        if (!nodeFound) {
            writer.write("FAILED" + "\n");
            writer.flush();
        }
    }

    //Handle GET? request from client
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
        dataStore.printContents();

        if (value == null) {
            writer.write("NOPE" + "\n");
            writer.flush();
        } else {
            int valueLines = value.split("\n").length;
            writer.write("VALUE " + valueLines + "\n" + value + "\n");
            writer.flush();
        }
    }

    //Handle END request from client
    private void handleEndRequest(String requesterNodeName, String requesterNodeAddress) throws IOException {
        //Remove client node to the map
        NetworkMap.removeNode(requesterNodeName, requesterNodeAddress);
        clientSocket.close();
    }
}