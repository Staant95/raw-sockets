package com.stasapp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class App {
     public static void main(String[] args) throws InterruptedException, ExecutionException {
        final int PORT = 8080;
        final String HOST = "localhost";

        var server = new Server();
        var bob = new Client("Bob", PORT, HOST);
        var jane = new Client("Jane", PORT, HOST, 5000);

        CompletableFuture.allOf(
            CompletableFuture.runAsync(server::start),
            CompletableFuture.runAsync(bob::start),
            CompletableFuture.runAsync(jane::start)
        ).get();

    }
}
