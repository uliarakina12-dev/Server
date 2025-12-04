package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool;
    private final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js"
    );

    public Server(int port, int poolSize) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port + " with thread pool of 64 threads");

            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                socket; // auto-close
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            System.out.println(Thread.currentThread().getName() + " → " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length != 3) return;

            String method = parts[0];
            String fullPath = parts[1];

            // Разделяем путь и query
            String path = fullPath;
            String query = null;
            int queryIndex = fullPath.indexOf('?');
            if (queryIndex > 0) {
                path = fullPath.substring(0, queryIndex);
                query = fullPath.substring(queryIndex + 1);
            }

            if (path.endsWith("/")) {
                path = "/index.html";
            }

            Request request = new Request(method, path, query);

            if (!validPaths.contains(request.getPath())) {
                sendNotFound(out);
                return;
            }

            if (request.getPath().equals("/classic.html")) {
                handleClassicHtml(out);
            } else {
                serveStaticFile(request.getPath(), out);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serveStaticFile(String path, BufferedOutputStream out) throws IOException {
        Path filePath = Path.of(".", "public", path);
        if (!Files.exists(filePath)) {
            sendNotFound(out);
            return;
        }

        String mimeType = Files.probeContentType(filePath);
        long length = Files.size(filePath);

        out.write(("""
                HTTP/1.1 200 OK
                Content-Type: %s
                Content-Length: %d
                Connection: close
                
                """.formatted(mimeType, length)).getBytes());

        Files.copy(filePath, out);
        out.flush();
    }

    private void handleClassicHtml(BufferedOutputStream out) throws IOException {
        Path filePath = Path.of(".", "public", "classic.html");
        String template = Files.readString(filePath);
        String content = template.replace("{time}", LocalDateTime.now().toString());

        byte[] body = content.getBytes();
        out.write(("""
                HTTP/1.1 200 OK
                Content-Type: text/html; charset=utf-8
                Content-Length: %d
                Connection: close
                
                """.formatted(body.length)).getBytes());
        out.write(body);
        out.flush();
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write("""
                HTTP/1.1 404 Not Found
                Content-Length: 0
                Connection: close
                
                """.getBytes());
        out.flush();
    }

    public void shutdown() {
        threadPool.shutdown();
        System.out.println("Server stopped.");
    }
}
