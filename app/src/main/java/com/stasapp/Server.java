package com.stasapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Server {

  private final Map<Integer, Server.Client> clients = new HashMap<>();

  private final int port;

  public Server(int port) {
    this.port = port;
  }

  public Server() {
    this.port = 8080;
  }


  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server started");

      while(true) {
        // Start accepting connections
        // The ServerSocket is blocking, so this will wait until a client connects
        // each time a client connects, it will create a new socket
        // Each client will be handled by a new thread
        Socket client = serverSocket.accept();

        CompletableFuture.runAsync(() -> handleClient(client));
      }

    } catch (IOException e) {
      System.out.println("Server exception: " + e.getMessage());
      e.printStackTrace();
    }
  }


  private void registerClient(String message, Socket client) throws IOException {
    if(message.startsWith("NICKNAME:")) {
      String nickname = message.split(":")[1];
      var newClient = new Server.Client(client, nickname);
      clients.put(client.hashCode(), newClient);
    } else {
      System.out.println("Client did not send a nickname first. Closing connection.");
      client.close();
    }
  }


  private void handleClient(Socket client) {
    try {

      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

      String message;

      var messageCounter = 0;

      while((message = in.readLine()) != null) {

        if(messageCounter == 0) {
          if(message.startsWith("NICKNAME:")) {
            this.registerClient(message, client);
          } else {
            System.out.println("Client did not send a nickname first. Closing connection.");
            break;
          }
        }

        spreadMessageToAllClients(client.hashCode(), message);

        messageCounter++;
      }

      // If we get here, the client has disconnected
      client.close();

    } catch(IOException e) {
      System.out.println("Server exception: " + e.getMessage());
      Thread.currentThread().interrupt();
    }

  }


  private void spreadMessageToAllClients(int senderHash, String message) {
    for(var client: clients.entrySet()) {

      if(client.getKey() != senderHash) {

        try {

          PrintWriter out = new PrintWriter(client.getValue().getSocket().getOutputStream(), true);
          out.println(
            formatMessage(client.getValue().getNickname(), message)
          );

        } catch(IOException e) {
          System.out.println("Server exception: " + e.getMessage());
          Thread.currentThread().interrupt();
        }

      }
    }
  }

  private String formatMessage(String nickname, String message) {
    return String.format("\u001B[34m%s > %s\u001B[0m", nickname, message);
  }



  private static final class Client {

    private final Socket socket;

    private final String nickname;


    public Client(Socket socket, String nickname) {
      this.socket = socket;
      this.nickname = nickname;
    }

    public Socket getSocket() {
      return socket;
    }

    public String getNickname() {
      return nickname;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((socket == null) ? 0 : socket.hashCode());
      result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Client other = (Client) obj;
      if (socket == null) {
        if (other.socket != null)
          return false;
      } else if (!socket.equals(other.socket))
        return false;
      if (nickname == null) {
        if (other.nickname != null)
          return false;
      } else if (!nickname.equals(other.nickname))
        return false;
      return true;
    }


  }


}
