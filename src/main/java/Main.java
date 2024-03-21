import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
    String directoryPath = "";
    ExecutorService executorService = Executors.newCachedThreadPool();
      for (int i = 0; i < args.length;  i++) {
          if(args[i].equalsIgnoreCase("--directory")) {
              directoryPath = args[i+1];
              break;
          }
      }

     try {
         serverSocket = new ServerSocket(4221);
         serverSocket.setReuseAddress(true);
         while (true) {
             clientSocket = serverSocket.accept(); // Wait for connection from client.
             if(clientSocket.isConnected()) {
                 //new ClientHandler(clientSocket).start();
                 Socket finalClientSocket = clientSocket;
                 String finalDirectoryPath = directoryPath;
                 executorService.submit(() -> handleClient(finalClientSocket, finalDirectoryPath));
             System.out.println("accepted new connection");
             }
         }
  } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
  public static void handleClient(Socket clientSocket, String directoryPath) {
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

          } else if(arg[0].equalsIgnoreCase("GET") && arg[1].startsWith("/files")){
              System.out.println("Directory request");
              String filename = arg[1].substring(7); //ignores first slash (/) as is already included iin directory path
              String filepath = directoryPath + filename;
              System.out.println("looking for: " + filepath);
              if(new File(filepath).exists()){
                  try(FileInputStream r = new FileInputStream(filepath)) {
                      byte[] content = r.readAllBytes();
                      var echo = new String(content);
                      httpResponse.append("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\n")
                              .append("Content-Length: " + content.length + "\r\n\r\n");
                      outputStream.write(httpResponse.toString().getBytes(StandardCharsets.UTF_8));
                      outputStream.write(content);
                      outputStream.flush();
                      clientSocket.close();
                      return;
                  }
              }else {
                  httpResponse.append("HTTP/1.1 404 BAD\r\n\r\n");
              }

          } else if (arg[0].equalsIgnoreCase("post") && arg[1].startsWith("/files")) {
              System.out.println("Directory POST request");
              String filename = arg[1].substring(7); //ignores first slash (/) as is already included iin directory path
              String filepath = directoryPath + filename;
              System.out.println("saving file: " + filepath);
              File newFile = new File(filepath);
              try(FileWriter fileWriter = new FileWriter(newFile)) {
                  String s;
                  StringBuilder requestBuilder = new StringBuilder();
                  String line;
                  while ((line = inputStreamReader.readLine()) != null && !line.isEmpty()) {
                      requestBuilder.append(line).append("\r\n");
                  }
                  requestBuilder.append("\r\n");
                  while (inputStreamReader.ready()) {
                      fileWriter.write((char)inputStreamReader.read());
                  }
              }
              httpResponse.append("HTTP/1.1 201 OK\r\n\r\n");
          }
          else {
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
