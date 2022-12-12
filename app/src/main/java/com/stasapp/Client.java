package com.stasapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Client {

  private final String nickname;

  private final int port;

  private final String host;

  private final int messageDelay;

  public Client(String nickname, int port, String host, int messageDelay) {
    this.nickname = nickname;
    this.port = port;
    this.host = host;
    this.messageDelay = messageDelay;
  }

  public Client(String nickname, int port, String host) {
    this(nickname, port, host, 3000);
  }

  public Client(String nickname) {
    this(nickname, 8080, "localhost", 3000);
  }

  public void start() {
    try {

      Socket client = new Socket(host, port);

      this.sendNickname(client);

      // handle reading and writing to the socket in new thread
      CompletableFuture.allOf(
        CompletableFuture.runAsync(() -> consumeMessages(client)),
        CompletableFuture.runAsync(() -> produceMessages(client, nickname))
      ).get();

    } catch (IOException | InterruptedException | ExecutionException e) {
      System.out.println("Client exception: " + e.getMessage());
      Thread.currentThread().interrupt();
    }
  }


  private void sendNickname(Socket client) {
    PrintWriter out = null;
    try {
      out = new PrintWriter(client.getOutputStream(), true);
      out.println("NICKNAME:" + nickname);
    } catch (IOException e) {
      System.out.println("Client exception while sending nickname: " + e.getMessage());
      Thread.currentThread().interrupt();
    }

  }


  private void consumeMessages(Socket client) {

    BufferedReader in = null;

    try {
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));

      String message = null;

      while((message = in.readLine()) != null) {
        System.out.println(message);
      }

    } catch (IOException e) {
      System.out.println("Client exception while consuming messages: " + e.getMessage());
      Thread.currentThread().interrupt();
    }
  }


  private void produceMessages(Socket client, String nickname) {
    int counter = 0;

    PrintWriter out = null;

    try {

      out = new PrintWriter(client.getOutputStream(), true);

      while(true) {
        out.println("Hello world " + counter++);
        Thread.sleep(messageDelay);
      }

    } catch(IOException | InterruptedException e) {
      System.out.println("Client exception while producing messages: " + e.getMessage());
      Thread.currentThread().interrupt();
    }

  }

}
