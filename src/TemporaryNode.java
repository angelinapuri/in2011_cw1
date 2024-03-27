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
    private NetworkMap networkMap;


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
                String response2 = readUntilEnd(reader);
                System.out.println(response2);
                if (response2.startsWith("SUCCESS")) {
                    return true;
                }
                // Return false if the store failed
                else if (response2.startsWith("FAILED")) {
                    return false;
                } else {
                    System.err.println("Unexpected response: " + response2);
                    return false;
                }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String get(String key) {
        try {
            // Send GET request
            String[] keyLines = key.split("\n");
            writer.write("GET? " + keyLines.length + "\n" + key + "\n");
            writer.flush();

            // Read GET response
            String response2 = readUntilEnd(reader);
            if (response2.startsWith("VALUE")) {
                return response2;
            } else {
                // Value not found
                return "NOPE";
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            return null;
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