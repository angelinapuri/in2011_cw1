// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Name: Angelina Puri
// Student ID: 220053946
// Email: angelina.puri@city.ac.uk

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Integer.parseInt;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws Exception;
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    private ServerSocket serverSocket;
    private Socket socket;
    private Writer writer;
    private BufferedReader reader;
    private static NetworkMap networkMap;
    private static DataStore dataStore;
    private final String nodeName = "angelina.puri@city.ac.uk:test-01";
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

    //Listen to incoming connections on given IP address and port number
    public boolean listen(String ipAddress, int portNumber) {
        try {
            this.ipAddress = ipAddress;
            this.portNumber = portNumber;
            //Create a server socket to listen to incoming connections
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening for incoming connections on " + ipAddress + ":" + portNumber);

            String nodeAddress = ipAddress + ":" + portNumber;
            //Add self to the network map
            NetworkMap.addNode(nodeName, nodeAddress);
            System.out.println("Added self to network map: " + nodeName + " at " + nodeAddress);

            //Check availability of nodes
            checkIfAlive();

            return true;
        } catch (IOException e) {
            System.err.println("Exception listening for incoming connections");
            e.printStackTrace();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        //Find and add nodes from the map
       sendNotifyRequests(startingNodeName, startingNodeAddress);
       findNodes(startingNodeName, startingNodeAddress);

        System.out.println("Connected to the network");

        //Handle multiple inbound connections
        while(true) {
            try {
                //Accept a new connection
                Socket acceptedSocket = serverSocket.accept();
                System.out.println("New connection accepted from " + acceptedSocket.getInetAddress().getHostAddress() + ":" + acceptedSocket.getPort());
                //Handle each connection in a new thread
                new Thread(new ClientHandler(acceptedSocket, networkMap, dataStore, nodeName, ipAddress, portNumber)).start();
            } catch (IOException e) {
                System.err.println("Error connecting to " + startingNodeAddress);
                System.err.println(e);
            }
        }
    }

    //Send a START message to the given node
    public void start(String startingNodeName, String startingNodeAddress) {
        try {
            //Connect to the starting node
            socket = new Socket(startingNodeAddress.split(":")[0], parseInt(startingNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            reader.readLine();

            writer.write("START 1 " + nodeName + "\n");
            writer.flush();

        } catch (Exception e) {
            System.err.println("IOException occurred: " + e.getMessage());
        }
    }

    //Find the nodes already present in the network map
    private void findNodes(String bootstrapNodeName, String bootstrapNodeAddress){
        try {
            socket = new Socket(bootstrapNodeAddress.split(":")[0], Integer.parseInt(bootstrapNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            start(bootstrapNodeName, bootstrapNodeAddress);

            //Iterate through characters to find the nearest nodes
            for(char c = 'a' ; c <= 'z' ; c++){
                writer.write("NEAREST? " + HashID.computeHashID(c + "\n") + "\n");
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

    //Send NOTIFY? request to the given node and add it to the network map
    private void sendNotifyRequests(String startingNodeName, String startingNodeAddress) {
        try {
            socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            start(startingNodeName, startingNodeAddress);

            writer.write("NOTIFY?" + "\n" + nodeName + "\n" + ipAddress + ":" + portNumber + "\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println(response);

            if (response != null && response.equals("NOTIFIED")) {
                writer.write("END: Notified Node");
                writer.flush();
                //Add the node to the network if correct response is received
                NetworkMap.addNode(startingNodeName, startingNodeAddress);
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Error sending notify request to " + startingNodeName + " at " + startingNodeAddress + ": " + e.getMessage());
        }
    }

    //Check availability of nodes in the network by sending an ECHO? request and updating the map every 60 seconds
    private void checkIfAlive(){
        Timer timer= new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (NodeNameAndAddress nodeNameAndAddress : NetworkMap.getMap().values()) {
                    String nodeToCheckName = nodeNameAndAddress.getNodeName();
                    String nodeToCheckAddress = nodeNameAndAddress.getNodeAddress();
                    if (!nodeToCheckName.equals(nodeName)) {

                        try {
                            socket = new Socket(nodeToCheckAddress.split(":")[0], Integer.parseInt(nodeToCheckAddress.split(":")[1]));
                            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            start(nodeToCheckName, nodeToCheckAddress);

                            writer.write("ECHO?" + "\n");
                            writer.flush();

                            String response = reader.readLine();
                            if (!response.equals("OHCE")) {
                                NetworkMap.removeNode(nodeToCheckName, nodeToCheckAddress);
                            }
                            else{
                                //System.out.println(nodeToCheckName + " at " + nodeToCheckAddress + " is alive!");
                            }
                            socket.close();
                        } catch (IOException ignored) {
                        }

                    }
                }
                System.out.println("Network Map updated!");

            }
        }, 0, 60 * 1000);
    }
}