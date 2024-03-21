import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
         serverSocket = new ServerSocket(4221);
         serverSocket.setReuseAddress(true);
         while (true) {
             clientSocket = serverSocket.accept(); // Wait for connection from client.
             System.out.println("accepted new connection");
             String httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
             OutputStream outputStream = clientSocket.getOutputStream();
             outputStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
             outputStream.flush();
             outputStream.close();
         }
  } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
