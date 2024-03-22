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

    private Map<String, String> networkMap;

    public FullNode() {

        networkMap = new HashMap<>();
    }
    public boolean listen(String ipAddress, int portNumber) {
        try {
            //Start listening on given port
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening...");
            // Return true if the node can accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Node connected!");
            }
            // Return false otherwise
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean notify(String nodeName, String nodeAddress, String ipAddress, int portNumber) {
        try {
            Socket socket = new Socket(ipAddress, portNumber);
            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.write("NOTIFY?\n");
            writer.write(nodeName + "\n");
            writer.write(nodeAddress + "\n");
            writer.flush();

            String response = reader.readLine();
            if (response != null && response.equals("NOTIFIED")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
            writer = new OutputStreamWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.write("START 1 " + startingNodeName + "\n");
            writer.flush();

            // Return true if the 2D#4 network can be contacted
            String response = reader.readLine();
            if (response != null && response.startsWith("START 1 ")) {
                return true;
            }
            // Return false if the 2D#4 network can't be contacted
            else {
                //    System.err.println("Connection not established");
                return false;
            }
        } catch (Exception e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        }
    }
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        networkMap.put(startingNodeName, startingNodeAddress);

    }
}
