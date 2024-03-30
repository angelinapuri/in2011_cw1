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
            notifyOtherFullNodes(ipAddress, portNumber);

            // Initialize reader and writer for socket communication
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new OutputStreamWriter(socket.getOutputStream());

            // Read incoming message from starting node
            String message = reader.readLine();
            String[] messageParts = message.split(" ");

            // Close resources
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

    private void notifyOtherFullNodes(String ipAddress, int portNumber) throws IOException {
        try {
            // Open a socket to communicate with other nodes
            Socket notifySocket = new Socket(ipAddress, portNumber);
            writer = new OutputStreamWriter(notifySocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(notifySocket.getInputStream()));

            // Prepare and send the NOTIFY? request
            String nodeName = "angelina.puri@city.ac.uk:test-01";
            writer.write("NOTIFY?\n" + nodeName + (ipAddress + ":" + portNumber));
            System.out.println("NOTIFY?\n" + nodeName + (ipAddress + ":" + portNumber));
            writer.flush();

            // Read and process the response from other nodes
            String response = reader.readLine();
            System.out.println(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}