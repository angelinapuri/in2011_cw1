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

import static java.lang.Integer.parseInt;

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
    private static NetworkMap networkMap;
    private static DataStore dataStore;
    private String nodeName = "angelina.puri@city.ac.uk:test-01";
    private String ipAddress;
    private int portNumber;

    public FullNode(NetworkMap networkMap) {
        this.networkMap = networkMap;
        if (dataStore == null) {
            dataStore = DataStore.getInstance();
        }
    }


    public FullNode() {
        networkMap = new NetworkMap();
    }

    public boolean listen(String ipAddress, int portNumber) {
        try {
            this.ipAddress = ipAddress;
            this.portNumber = portNumber;

            //InetAddress host = InetAddress.getByName(ipAddress);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening for incoming connections on " + ipAddress + ":" + portNumber);

            String nodeAddress = ipAddress + ":" + portNumber;

            NetworkMap.addNode(nodeName, nodeAddress);
            System.out.println("Added self to network map: " + nodeName + " at " + nodeAddress);

            return true;

        } catch (IOException e) {
            System.err.println("Exception listening for incoming connections");
            e.printStackTrace();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        sendNotifyRequests(startingNodeName, startingNodeAddress);
        findNodes(startingNodeName, startingNodeAddress);

        List<NodeNameAndAddress> nodes = new ArrayList<>(NetworkMap.getMap().values());
        for (NodeNameAndAddress nodeNameAndAddress : nodes) {
            System.out.println(nodeNameAndAddress);
        }        System.out.println("Connected to the network");

        try {
            Socket acceptedSocket = serverSocket.accept();
            System.out.println("New connection accepted from " + acceptedSocket.getInetAddress().getHostAddress() + ":" + acceptedSocket.getPort());
            new Thread(new ClientHandler(acceptedSocket, networkMap, dataStore)).start();
        } catch (IOException e) {
            System.err.println("Error connecting to " + startingNodeAddress);
            System.err.println(e);
        }
    }

    public void start(String startingNodeName, String startingNodeAddress) {
        try {

            //Connect to the starting node
            socket = new Socket(startingNodeAddress.split(":")[0], parseInt(startingNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = reader.readLine();
            System.out.println(response);

            writer.write("START 1 angelina.puri@city.ac.uk:MyImplementation" + "\n");
            writer.flush();

        } catch (Exception e) {
            System.err.println("IOException occurred: " + e.getMessage());
        }
    }

    private void findNodes(String bootstrapNodeName, String bootstrapNodeAddress){
        try {
            socket = new Socket(bootstrapNodeAddress.split(":")[0], Integer.parseInt(bootstrapNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            start(bootstrapNodeName, bootstrapNodeAddress);

            for(char c = 'a' ; c <= 'z' ; c++){
                writer.write("NEAREST? " + HashID.computeHashID(c + "\n") + "\n");
                //System.out.println("NEAREST? " + HashID.computeHashID(c + "\n") + "\n");
                writer.flush();

                String response = reader.readLine();
                StringBuilder nodeInfoBuilder = new StringBuilder();
                int nodes = Integer.parseInt(response.split(" ")[1]);
                int nodeLines = (nodes * 2);
                if (response.startsWith("NODES")) {
                    nodeInfoBuilder.append(response).append("\n");
                    for (int i = 0; i < nodeLines; i++) {
                        String line = reader.readLine();
                        nodeInfoBuilder.append(line).append("\n");
                    }
                }
                String nearestNodesList = nodeInfoBuilder.toString().trim();
                //System.out.println(nearestNodesList);

                String[] nearestNodesLines = nearestNodesList.split("\n");
                for (int i = 1; i < nearestNodesLines.length; i += 2) {
                    String nearestNodeName = nearestNodesLines[i];
                    String nearestNodeAddress = nearestNodesLines[i + 1];
                    if (!NetworkMap.getMap().containsKey(nearestNodeName) || !NetworkMap.getMap().get(nearestNodeName).getNodeAddress().equals(nearestNodeAddress)) {
                        sendNotifyRequests(nearestNodeName, nearestNodeAddress);
                        findNodes(nearestNodeName, nearestNodeAddress);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error sending nearest request to " + bootstrapNodeAddress + " at " + bootstrapNodeAddress + ": " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendNotifyRequests(String startingNodeName, String startingNodeAddress) {
            try {
                socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                start(startingNodeName, startingNodeAddress);

                writer.write("NOTIFY?" + "\n" + nodeName + "\n" + ipAddress + ":" + portNumber + "\n");
                writer.flush();
                System.out.println("NOTIFY?" + "\n" + nodeName + "\n" + ipAddress + ":" + portNumber + "\n");

                System.out.println("Notify request sent to " + startingNodeName + " at " + startingNodeAddress);

                String response2 = reader.readLine();
                System.out.println(response2);

                if (response2 != null && response2.equals("NOTIFIED")) {
                    writer.write("END: Notified Node");
                    writer.flush();
                }

                NetworkMap.addNode(startingNodeName, startingNodeAddress);

                socket.close();
            } catch (IOException e) {
                System.err.println("Error sending notify request to " + startingNodeName + " at " + startingNodeAddress + ": " + e.getMessage());
            }
        }
    }