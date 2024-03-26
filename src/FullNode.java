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
            //Start listening on given port
            System.out.println("Listening...");
            ServerSocket serverSocket = new ServerSocket(portNumber);

            // Return true if the node can accept incoming connections
            Thread incomingConnectionsThread = new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Node connected!");
                        handleIncomingConnections(clientSocket);
                    } catch (IOException e) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }});
                incomingConnectionsThread.start();
                return true;
            }
            // Return false otherwise
          catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {

}
    private void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
            try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            String message = reader.readLine();
            String[] parts = message.split(" ");

            if (parts.length > 0) {
                String requestType = parts[0];
                switch (requestType) {
                    case "NOTIFY?":
                        handleNotifyRequest(startingNodeName, startingNodeAddress);
                        break;
                    case "ECHO":
                        handleEchoRequest(writer);
                        break;
                    case "PUT?":
                        handlePutRequest(reader, writer, parts);
                        break;
                    case "GET?":
                        handleGetRequest(writer, parts);
                        break;
                    default:
                        writer.write("Invalid request");
                        writer.flush();
                        break;
                }
            }

            // Close resources
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

    private void handleNotifyRequest(String startingNodeName, String startingNodeAddress) throws IOException {
    networkMap.add(startingNodeName, startingNodeAddress);
        writer.write("NOTIFIED");
        writer.flush();
    }

    private void handleEchoRequest(Writer writer) throws IOException {
        writer.write("OHCE");
        writer.flush();
    }

    private void handlePutRequest(BufferedReader reader, Writer writer, String[] parts) throws IOException {
        if (parts.length == 3 && parts[0].equals("PUT?")) {
            String keyLines = parts[1];
            String valueLines = parts[2];
            int keyLineCount = Integer.parseInt(keyLines);
            int valueLineCount = Integer.parseInt(valueLines);
            StringBuilder keyBuilder = new StringBuilder();
            StringBuilder valueBuilder = new StringBuilder();

            // Read the key lines
            for (int i = 0; i < keyLineCount; i++) {
                String line = reader.readLine();
                if (line == null) {
                    writer.write("FAILED: Incomplete key");
                    writer.flush();
                    return;
                }
                keyBuilder.append(line).append("\n");
            }

            // Read the value lines
            for (int i = 0; i < valueLineCount; i++) {
                String line = reader.readLine();
                if (line == null) {
                    writer.write("FAILED: Incomplete value");
                    writer.flush();
                    return;
                }
                valueBuilder.append(line).append("\n");
            }

            // Store the key-value pair
            String key = keyBuilder.toString().trim();
            String value = valueBuilder.toString().trim();
            dataStore.store(key, value);
            writer.write("SUCCESS");
            writer.flush();
        writer.write("SUCCESS");
        writer.flush();
    }
}

    private void handleGetRequest(Writer writer, String[] parts) throws IOException {
        if (parts.length == 2 && parts[0].equals("GET?")) {
            String key = parts[1];
            String value = dataStore.get(key);
            if (value != null) {
                writer.write("VALUE " + value.length());
                writer.write(value);

            } else {
                writer.write("");
            }
        } else {
            writer.write("Invalid message format");
        }

        writer.flush();
    }
}