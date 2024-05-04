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

    public boolean listen(String ipAddress, int portNumber) {
        try {
            this.ipAddress = ipAddress;
            this.portNumber = portNumber;

            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening for incoming connections on " + ipAddress + ":" + portNumber);

            String nodeAddress = ipAddress + ":" + portNumber;

            NetworkMap.addNode(nodeName, nodeAddress);
            System.out.println("Added self to network map: " + nodeName + " at " + nodeAddress);

            if(checkIfAlive()){
                System.out.println("Network Map updated!");
            }

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

//     Print out the network map
//        List<NodeNameAndAddress> nodes = new ArrayList<>(NetworkMap.getMap().values());
//        for (NodeNameAndAddress nodeNameAndAddress : nodes) {
//            System.out.println(nodeNameAndAddress);
//        }

        System.out.println("Connected to the network");

        while(true) {
            try {
                Socket acceptedSocket = serverSocket.accept();
                System.out.println("New connection accepted from " + acceptedSocket.getInetAddress().getHostAddress() + ":" + acceptedSocket.getPort());
                new Thread(new ClientHandler(acceptedSocket, networkMap, dataStore, nodeName, ipAddress, portNumber)).start();
            } catch (IOException e) {
                System.err.println("Error connecting to " + startingNodeAddress);
                System.err.println(e);
            }
        }
    }

    public void start(String startingNodeName, String startingNodeAddress) {
        try {

            //Connect to the starting node
            socket = new Socket(startingNodeAddress.split(":")[0], parseInt(startingNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = reader.readLine();

            writer.write("START 1 " + nodeName + "\n");
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

            String response = reader.readLine();
            System.out.println(response);

            if (response != null && response.equals("NOTIFIED")) {
                writer.write("END: Notified Node");
                writer.flush();
                NetworkMap.addNode(startingNodeName, startingNodeAddress);
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Error sending notify request to " + startingNodeName + " at " + startingNodeAddress + ": " + e.getMessage());
        }
    }

    private boolean checkIfAlive(){
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
            }
        }, 0, 60 * 1000);
        return true;
    }
}