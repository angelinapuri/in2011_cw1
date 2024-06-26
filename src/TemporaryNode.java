// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Name: Angelina Puri
// Student ID: 220053946
//// Email: angelina.puri@city.ac.uk

import java.io.*;
import java.net.Socket;
import java.lang.StringBuilder;

import static java.lang.Integer.parseInt;

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

            //Connect to the starting node
            socket = new Socket(startingNodeAddress.split(":")[0], parseInt(startingNodeAddress.split(":")[1]));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = reader.readLine();
            System.out.println(response);

            writer.write("START 1 angelina.puri@city.ac.uk:MyImplementation" + "\n");
            writer.flush();

            // Return true if the 2D#4 network can be contacted
            return response != null && response.startsWith("START 1 ");
        } catch (Exception e) {
            // Return false if the 2D#4 network can't be contacted
            System.err.println("IOException occurred: " + e.getMessage());
            return false;
        }
    }


    public boolean store(String key, String value) {
        try {
            System.out.println(nearest(key)); //Print the list of nodes nearest to the key (Even if the response is SUCCESS,
                                              // the node may not be the closest one to the key's HashID.
                                              // So the node has to send PUT? requests to each of the nearest nodes.
            //Send a PUT? request
            String[] keyLines = key.split("\n");
            String[] valueLines = value.split("\n");
            writer.write("PUT? " + keyLines.length + " " + valueLines.length + "\n" + key + value);
            writer.flush();

            //Return true if the store worked
            String response = reader.readLine();
            if (response.equals("SUCCESS")) {
                return true;
            }
            // Return false if the store failed
            else if (response.equals("FAILED")) {
                return false;
            }
        }
        catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            return false;
        }
        finally {
            try {
                    writer.write("END: EndOfRequest");
                    writer.flush();
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String get(String key) {
        try {
                //Send a GET? request
                String[] keyLines = key.split("\n");
                writer.write("GET? " + keyLines.length + "\n" + key);
                writer.flush();

                // Read GET response
                String response = reader.readLine();
                StringBuilder valueBuilder = new StringBuilder();

                //Return value receive
                if (response.startsWith("VALUE")) {
                    valueBuilder.append(response).append("\n");
                    int valueLines = Integer.parseInt(response.split(" ")[1]);
                    for (int i = 0; i < valueLines; i++) {
                        String line = reader.readLine();
                        valueBuilder.append(line).append("\n");
                    }
                    return valueBuilder.toString().trim();
                }
                else {
                    // Value not found
                    System.out.println(nearest(key)); //Print the list of nodes nearest to the key
                    return response;
                }
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            return null;
        }
        finally {
            try {
                writer.write("END: EndOfRequest");
                System.out.println("END: EndOfRequest");
                writer.flush();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String nearest(String string) throws Exception {

        // Send NEAREST request
        writer.write("NEAREST? " + HashID.computeHashID(string) + "\n");
        writer.flush();

        //Build response returned from nearest request
        String response = reader.readLine();
        StringBuilder nodeInfoBuilder = new StringBuilder();
        int nodes = Integer.parseInt(response.split(" ")[1]);
        int nodeLines = (nodes*2);
        if (response.startsWith("NODES")) {
            nodeInfoBuilder.append(response).append("\n");
            for (int i = 0; i < nodeLines; i++) {
                String line = reader.readLine();
                nodeInfoBuilder.append(line).append("\n");
            }
        }
        return nodeInfoBuilder.toString().trim();
    }
}

/** Have nearest in get and store methods. So, if nearest call garda it returns itself, then
 * successful otherwise failed bhanera but still prints all the nodes bhanum.*/
