// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Angelina Puri
// 220053946
// angelina.puri@city.ac.uk

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.lang.StringBuilder;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.System.out;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    private Socket socket;
    private Writer writer;
    private BufferedReader reader;


    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            //Connect to the starting node
            socket = new Socket(startingNodeAddress.split(":")[0], parseInt(startingNodeAddress.split(":")[1]));
            writer = new OutputStreamWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.write("START 1 angelina.puri@city.ac.uk:MyImplementation" + "\n");
            writer.flush();

            // Return true if the 2D#4 network can be contacted
            String response = reader.readLine();
            // Return false if the 2D#4 network can't be contacted
            System.out.println(response);
            return response != null && response.startsWith("START 1 ");
        } catch (Exception e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        try {
            String[] keyLines = key.split("\n");
            String[] valueLines = value.split("\n");
            writer.write("PUT? " + keyLines.length + " " + valueLines.length + "\n" + key + "\n" + value + "\n");
            writer.flush();

            //Return true if the store worked
            String response = readUntilEnd(reader);
            if (response.startsWith("SUCCESS")) {
                return true;
            }
            // Return false if the store failed
            else if (response.startsWith("FAILED")) {
                return false;
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        }
        return false;
    }


    public String get(String key) {
        try {
            // Send GET request
            String[] keyLines = key.split("\n");
            writer.write("GET? " + keyLines.length + "\n" + key + "\n");
            writer.flush();

            // Read GET response
            String response = readUntilEnd(reader);
            if (response.startsWith("VALUE")) {
                return response;
            } else {
                // Value not found
                return "NOPE";
            }
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            return null;
        }
    }
    private String readUntilEnd(BufferedReader reader) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line).append("\n");
        }
        return responseBuilder.toString().trim();
    }
}






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
            // Start listening on given port
            System.out.println("Listening...");
            serverSocket = new ServerSocket(portNumber);

            // Return true if the node can accept incoming connections
            Thread incomingConnectionsThread = new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Node connected!");
                        String startingNodeName = clientSocket.getInetAddress().getHostName();
                        String startingNodeAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                        handleIncomingConnections(startingNodeName, startingNodeAddress);
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Writer writer = new OutputStreamWriter(socket.getOutputStream())) {

            String request = reader.readLine();
            String[] parts = request.split(" ");


            if (request.equals("START 1 " + startingNodeName)) {
                writer.write("START 1 ");
                writer.flush();
            }

            else if (request.equals("NOTIFY?" + "\n" + startingNodeName + "\n" + startingNodeAddress)) {
                writer.write("NOTIFIED");
                writer.flush();
            }

            else if (request.equals("ECHO?")) {
                writer.write("OHCE");
                writer.flush();
            }

            else if (parts.length == 3 && parts[0].equals("PUT?")) {
                int keyLineCount = Integer.parseInt(parts[1]);
                int valueLineCount = Integer.parseInt(parts[2]);

                StringBuilder keyBuilder = new StringBuilder();
                StringBuilder valueBuilder = new StringBuilder();

                // Read the key lines
                for (int i = 0; i < keyLineCount; i++) {
                    String line = reader.readLine();
                    if (line == null) {
                        writer.write("FAILED");
                        writer.flush();
                        return;
                    }
                    keyBuilder.append(line).append("\n");
                }

                // Read the value lines
                for (int i = 0; i < valueLineCount; i++) {
                    String line = reader.readLine();
                    if (line == null) {
                        writer.write("FAILED");
                        writer.flush();
                        return;
                    }
                    valueBuilder.append(line).append("\n");
                }

                String key = HashID.computeHashID(keyBuilder.toString().trim());
                String value = HashID.computeHashID(valueBuilder.toString().trim());
                dataStore.store(key, value);
                writer.write("SUCCESS");
                writer.flush();
            }

            else if (parts.length == 2 && parts[0].equals("GET?")) {
                int keyLineCount = Integer.parseInt(parts[1]);

                StringBuilder keyBuilder = new StringBuilder();

                for (int i = 0; i < keyLineCount; i++) {
                    String line = reader.readLine();
                    if (line == null) {
                        writer.write("FAILED");
                        writer.flush();
                        return;
                    }
                    keyBuilder.append(line).append("\n");
                }
                String key = HashID.computeHashID(keyBuilder.toString().trim());
                writer.write(dataStore.get(key));
                writer.flush();
            }

            else if (parts.length == 2 && parts[0].equals("GET?")) {
                int keyLineCount = Integer.parseInt(parts[1]);

                StringBuilder keyBuilder = new StringBuilder();

                for (int i = 0; i < keyLineCount; i++) {
                    String line = reader.readLine();
                    if (line == null) {
                        writer.write("FAILED");
                        writer.flush();
                        return;
                    }
                    keyBuilder.append(line).append("\n");
                }
                String key = HashID.computeHashID(keyBuilder.toString().trim());
                writer.write(dataStore.get(key));
                writer.flush();
            }

            else if (request.equals(("NEAREST?" ) +
                    HashID.computeHashID(startingNodeName+startingNodeAddress))) {

                writer.flush();
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}