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
            sendNotifyRequests(nodeName, nodeAddress);

            while (true) {
                Socket acceptedSocket = serverSocket.accept();
                System.out.println("New connection accepted");
                handleIncomingConnections(nodeName, nodeAddress);
                new Thread(new ClientHandler(acceptedSocket, networkMap, dataStore)).start();
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
            System.out.println(reader.readLine());
        } catch (IOException e) {
            System.err.println("Error sending START message: " + e.getMessage());
        }
    }

    private void sendNotifyRequest(String targetNodeName, String targetNodeAddress, String startingNodeName, String startingNodeAddress) {
        try {

            // Send START message
            sendStartMessage(targetNodeName, targetNodeAddress, startingNodeName, startingNodeAddress);

            writer.write("NOTIFY?" + "\n" + startingNodeName + "\n" + startingNodeAddress + "\n");
            writer.flush();
            System.out.println("NOTIFY?" + "\n" + startingNodeName + "\n" + startingNodeAddress + "\n");

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
}