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
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, String> networkMap;

    public TemporaryNode() {
        networkMap = new HashMap<>();
    }

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            //Connect to the starting node
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
                return false;
            }
        } catch (Exception e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        int keyLines = key.split("\n").length;
        int valueLines = value.split("\n").length;

        try {
            String keyHash = HashID.computeHashID(key);
            FullNode closestNode = Nearest(keyHash);

            Socket fullNodeSocket = new Socket(closestNode.getAddress().split(":")[0],
                    Integer.parseInt(closestNode.getAddress().split(":")[1]));
            Writer fullNodeWriter = new OutputStreamWriter(fullNodeSocket.getOutputStream());
            BufferedReader fullNodeReader = new BufferedReader(
                    new InputStreamReader(fullNodeSocket.getInputStream()));

            fullNodeWriter.write("PUT? " + keyLines + " " + valueLines + "\n");
            fullNodeWriter.write(key + "\n");
            fullNodeWriter.write(value);
            fullNodeWriter.flush();

            //Return true if the store worked
            String response = fullNodeReader.readLine();
            if (response.equals("SUCCESS")) {
                return true;

            }
            // Return false if the store failed
            else if (response.equals("FAILED")) {
                return false;
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }


    public String get(String key) {
        try {
            int keyLines = key.split("\n").length;

            String keyHash = HashID.computeHashID(key);
            FullNode closestNode = Nearest(HashID.computeHashID(key));

            Socket fullNodeSocket = new Socket(closestNode.getAddress().split(":")[0],
                    Integer.parseInt(closestNode.getAddress().split(":")[1]));
            Writer fullNodeWriter = new OutputStreamWriter(fullNodeSocket.getOutputStream());
            BufferedReader fullNodeReader = new BufferedReader(
                    new InputStreamReader(fullNodeSocket.getInputStream()));


            // Return the string if the get worked
            fullNodeWriter.write("GET? " + keyLines + "\n");
            fullNodeWriter.write(key + "\n");
            fullNodeWriter.flush();

            String response = fullNodeReader.readLine();
            // Return the string if the get worked
            if (response != null && response.startsWith("VALUE ")) {
                return response;
            }
            // Return null if it didn't
            else{
                return "Not implemented";
            }

        } catch (Exception e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return null;
        }
    }
}


//hi