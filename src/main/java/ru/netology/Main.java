package ru.netology;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
  private static final int PORT = 9999;
  private static final int THREAD_POOL_SIZE = 64;

  public static void main(String[] args) {
    final var server = new Server(PORT, THREAD_POOL_SIZE);
    server.start();
  }
}


