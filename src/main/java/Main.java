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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    ExecutorService executorService = Executors.newCachedThreadPool();

      for (var s:args
           ) {
          System.out.println(s);

      }

     try {
         serverSocket = new ServerSocket(4221);
         serverSocket.setReuseAddress(true);
         while (true) {
             clientSocket = serverSocket.accept(); // Wait for connection from client.
             if(clientSocket.isConnected()) {
                 //new ClientHandler(clientSocket).start();
                 Socket finalClientSocket = clientSocket;
                 executorService.submit(() -> handleClient(finalClientSocket));
             System.out.println("accepted new connection");
             }
         }
  } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
  public static void handleClient(Socket clientSocket) {
      try (BufferedReader inputStreamReader = new BufferedReader(
              new InputStreamReader(clientSocket.getInputStream()));
           OutputStream outputStream = clientSocket.getOutputStream();
      ) {
          String[] arg = inputStreamReader.readLine().split(" ");
          System.out.println(Arrays.toString(arg));
          StringBuilder httpResponse = new StringBuilder();
          if(arg[1].equals("/")) {
              httpResponse.append("HTTP/1.1 200 OK\r\n\r\n");
          } else if(arg[1].startsWith("/echo/")) {
              String echo = arg[1].substring(6);
              httpResponse.append("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n")
                      .append("Content-Length: " + echo.length() + "\r\n\r\n")
                      .append(echo);

          } else if(arg[1].equals("/user-agent")) {
              String s;
              while(!(s = inputStreamReader.readLine()).isEmpty()) {
                  arg = s.split(" ");
                  if(arg[0].equalsIgnoreCase("USER-AGENT:")) {
                      break;
                  }
              }
              String echo = arg[1];
              httpResponse.append("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n")
                      .append("Content-Length: " + echo.length() + "\r\n\r\n")
                      .append(echo);

          } else {
              httpResponse.append("HTTP/1.1 404 BAD\r\n\r\n");
          }

          outputStream.write(httpResponse.toString().getBytes(StandardCharsets.UTF_8));
          outputStream.flush();
          clientSocket.close();
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
  }
}
