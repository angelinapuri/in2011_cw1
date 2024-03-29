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

        // Instance variables for starting node info
        private String startingNodeName;
        private String startingNodeAddress;

        public boolean start(String startingNodeName, String startingNodeAddress) {
            try {
                // Set starting node info
                this.startingNodeName = startingNodeName;
                this.startingNodeAddress = startingNodeAddress;

                //Connect to the starting node
                socket = new Socket(startingNodeAddress.split(":")[0], parseInt(startingNodeAddress.split(":")[1]));
                writer = new OutputStreamWriter(socket.getOutputStream());
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
            writer.write("PUT? " + keyLines.length + " " + valueLines.length + "\n" + key + "\n" + value + "\n");
            writer.flush();

            //Return true if the store worked
            String response = readUntilEnd(reader);
            if (response.startsWith("SUCCESS")) {
                return true;
            }
            // Return false if the store failed
            else if (response.startsWith("FAILED")) {
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
            System.out.println(key);
            writer.write("NEAREST? " + "11c87226f0053e20df90aef2b6005e92b58580e186e543ccf4d4f34336ac1c53"));
                System.out.println("NEAREST? " + "11c87226f0053e20df90aef2b6005e92b58580e186e543ccf4d4f34336ac1c53"));
                writer.flush();

                // Read NEAREST response
                String response = reader.readLine();
                if(response.startsWith("NODES")){
                System.out.println(response);
                return response;
                }
        else{
            return null;
        }


            /**   // Check if the response starts with "NODES"
               if (response.startsWith("NODES")) {
                   String[] lines = response.split("\n");
                   firstNodeName = lines[1].trim().split(",")[0];
                   firstNodeAddress = lines[2].trim();
                   System.out.println(response);
                   break;
               } */
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            return null;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

/** Have nearest in get and store methods. So, if nearest call garda it returns itself, then
 * successful otherwise failed bhanera but still prints all the nodes bhanum.*/
