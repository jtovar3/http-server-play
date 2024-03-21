import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
         serverSocket = new ServerSocket(4221);
         serverSocket.setReuseAddress(true);
         while (true) {
             clientSocket = serverSocket.accept(); // Wait for connection from client.
             if(clientSocket.isConnected()) {
                 new ClientHandler(clientSocket).start();
             System.out.println("accepted new connection");
             }
         }
  } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
