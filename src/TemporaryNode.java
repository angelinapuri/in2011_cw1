// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Angelina Puri
// 220053946
// angelina.puri@city.ac.uk

import java.io.*;
import java.net.Socket;
import java.lang.StringBuilder;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import static java.lang.System.setOut;

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

                writer.write("START 1 " + startingNodeName + ":" + startingNodeAddress + "\n");
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
            writer.write("PUT? " + keyLines.length + " " + valueLines.length + "\n");
            writer.write(key + "\n");
            writer.write(value + "\n");
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
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            return false;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public String get(String key) {
        try {
            String[] keyLines = key.split("\n");
            writer.write("GET? " + keyLines.length + "\n" + key + "\n");
            writer.flush();

            // Read GET response
            String response = reader.readLine();
            StringBuilder valueBuilder = new StringBuilder();

            if (response.startsWith("VALUE")) {
                valueBuilder.append(response).append("\n"); // Append the first line

                int valueLines = Integer.parseInt(response.split(" ")[1]);
                for (int i = 0; i < valueLines; i++) {
                    String line = reader.readLine();
                    valueBuilder.append(line).append("\n");
                }
                return valueBuilder.toString().trim();
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
}

/** Have nearest in get and store methods. So, if nearest call garda it returns itself, then
 * successful otherwise failed bhanera but still prints all the nodes bhanum.*/
