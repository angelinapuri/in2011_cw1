// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.*;
import java.net.InetAddress;
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
            //InetAddress host = InetAddress.getByName(ipAddress);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening for incoming connections on " + ipAddress + ":" + portNumber);
            String nodeName = "angelina.puri@city.ac.uk:test-01";
            String nodeAddress = ipAddress + ":" + portNumber;
            NetworkMap.addNode(nodeName, nodeAddress);
            System.out.println("Added self as a node: " + nodeName + " at " + nodeAddress);
            sendNotifyRequests(nodeName, nodeAddress);
            System.out.println("Connected to the network");
            List<NodeNameAndAddress> nodes = new ArrayList<>(NetworkMap.getMap().values());
            for (NodeNameAndAddress nodeNameAndAddress : nodes) {
                System.out.println(nodeNameAndAddress);
            }
            
            return true;

        } catch (IOException e) {
            System.err.println("Exception listening for incoming connections");
            e.printStackTrace();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        try {
            Socket acceptedSocket = serverSocket.accept();
            System.out.println("New connection accepted from " + acceptedSocket.getInetAddress().getHostAddress() + ":" + acceptedSocket.getPort());
            new Thread(new ClientHandler(acceptedSocket, networkMap, dataStore)).start();
        } catch (IOException e) {
            System.err.println("Error connecting to " + startingNodeAddress);
            System.err.println(e);
        }
    }

    private void sendNotifyRequests(String startingNodeName, String startingNodeAddress) {
        List<NodeNameAndAddress> nodes = new ArrayList<>(NetworkMap.getMap().values());
        for (NodeNameAndAddress nodeNameAndAddress : nodes) {
            String nodeName = nodeNameAndAddress.getNodeName();
            String nodeAddress = nodeNameAndAddress.getNodeAddress();

            if(nodeName.startsWith(startingNodeName) && nodeAddress.equals(startingNodeAddress)){
                continue;
            }

            try {
                Socket socket = new Socket(nodeAddress.split(":")[0], Integer.parseInt(nodeAddress.split(":")[1]));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                writer.write("START 1 " + startingNodeName + "\n");
                System.out.println("START 1 " + startingNodeName + "\n");
                writer.flush();

                String response = reader.readLine();
                System.out.println(response);

                if (response != null && response.startsWith("START 1 ")) {
                    writer.write("NOTIFY?" + "\n" + startingNodeName + "\n" + startingNodeAddress + "\n");
                    writer.flush();
                    System.out.println("NOTIFY?" + "\n" + startingNodeName + "\n" + startingNodeAddress + "\n");

                    System.out.println("Notify request sent to " + nodeName + " at " + nodeAddress);

                    String response2 = reader.readLine();
                    System.out.println(response2);

                    if (response2 != null && response2.equals("NOTIFIED")) {
                        writer.write("END: Notified Node");
                        writer.flush();
                    }
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("Error sending notify request to " + nodeName + " at " + nodeAddress + ": " + e.getMessage());
            }
        }
    }

}