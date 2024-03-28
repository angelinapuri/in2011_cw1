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
            // Connect to the starting node
            socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
            writer = new OutputStreamWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send start message to the starting node
            writer.write("START 1 " + startingNodeName + "\n");
            writer.flush();

            // Return true if the network can be contacted
            String response = reader.readLine();
            return response != null && response.startsWith("START 1 ");
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        int keyLines = key.split("\n").length;
        int valueLines = value.split("\n").length;
        try {
            writer.write("PUT? " + keyLines + " " + valueLines + "\n");
            writer.write(key + "\n");
            writer.write(value);
            writer.flush();

            //Return true if the store worked
            String response = reader.readLine();
            if (response.equals("SUCCESS")) {
                return true;
            }
            // Return false if the store failed
            else if (response.equals("FAILED")) {
                return false;
            } else {
                System.err.println("Unexpected response: " + response);
                return false;
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        }
    }


    public String get(String key) {
        int keyLines = key.split("\n").length;
        try {
            // Return the string if the get worked
            writer.write("GET? " + keyLines);
            writer.write(key + "\n");
            writer.flush();

            String response = reader.readLine();
            // Return the string if the get worked
            if (response != null && response.startsWith("VALUE ")) {
                return response;
            }

            // Return null if it didn't
            else {
                return "NOPE";
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return null;
        }
    }
}