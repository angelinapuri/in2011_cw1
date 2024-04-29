import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {

    public HTTPServer() {}

    public static void main(String[] args) throws IOException {

        // IP Addresses will be discussed in detail in lecture 4
        String IPAddressString = "127.0.0.1";
        InetAddress host = InetAddress.getByName(IPAddressString);
        // Port numbers will be discussed in detail in lecture 5
        int port = 8080;

        // The server side is slightly more complex
        // First we have to create a ServerSocket
        System.out.println("Opening the server socket on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        // The ServerSocket listens and then creates as Socket object
        // for each incoming connection
        System.out.println("Server waiting for client...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected!");

        // Like files, we use readers and writers for convenience
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

        // Parse the HTTP request
        String request = reader.readLine();
        String parts[] = request.split(" ");
        String method = parts[0];

        if (method.equals("GET")) {
            String path = parts[1];

            System.out.println("GETting the file " + path);

            // Read the headers
            do {
                String header = reader.readLine();
                if (header.equals("")) break;
                // Could split the headers using ":" and save them in a map
            } while (true);

            if (path.equals("/") || path.equals("/index.html")) {
                String simpleHTML = "<html>\r\n<head>\r\n<title>Hello World</title>\r\n<body>\r\n<h1>HELLO WORLD!</h1>\r\n</body>\r\n</html>\r\n";

                writer.write("HTTP/1.1 200 OK\r\n");
                writer.write("Content-Type: text/html\r\n");
                writer.write("Content-Length: " + simpleHTML.length() + "\r\n");
                writer.write("\r\n");
                writer.write(simpleHTML);

                writer.flush();

            } else {
                String errorPage = "<html>\r\n<head>\r\n<title>Not Found</title>\r\n<body>\r\n<h1>File not found :-(</h1>\r\n</body>\r\n</html>\r\n";

                writer.write("HTTP/1.1 404 Not Found\r\n");
                writer.write("Content-Type: text/html\r\n");
                writer.write("Content-Length: " + errorPage.length() + "\r\n");
                writer.write("\r\n");
                writer.write(errorPage);

                writer.flush();
            }
        } else {
            // Could implement other methods here
        }

        // Close down the connection
        clientSocket.close();
    }
}