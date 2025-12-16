import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = parseIntEnv("WEB_PORT").orElse(9300);
        String bindAddress = Optional.ofNullable(System.getenv("WEB_ADDRESS")).orElse("0.0.0.0");
        String enrollmentApi = Optional.ofNullable(System.getenv("ENROLLMENT_API_URL"))
                .orElse("http://127.0.0.1:9200/enrollments");

        HttpServer server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
        HttpClient client = HttpClient.newHttpClient();

        server.createContext("/", exchange -> {
            redirect(exchange, "/enroll.html");
        });

        server.createContext("/enroll.html", new StaticHandler("enrollment-webapp/static/enroll.html"));

        server.createContext("/enroll", new ProxyEnrollHandler(client, enrollmentApi));

        server.createContext("/health", exchange -> {
            byte[] body = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
            addCors(exchange);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
        });

        server.start();
        System.out.println("Enrollment WebApp listening on http://" + bindAddress + ":" + port);
        System.out.println("Proxy target: " + enrollmentApi);
    }

    static Optional<Integer> parseIntEnv(String name) {
        try { return Optional.of(Integer.parseInt(System.getenv(name))); } catch (Exception e) { return Optional.empty(); }
    }

    static void redirect(HttpExchange exchange, String to) throws IOException {
        exchange.getResponseHeaders().add("Location", to);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    static class StaticHandler implements HttpHandler {
        private final Path filePath;
        StaticHandler(String path) { this.filePath = Path.of(path); }
        @Override public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); exchange.close(); return;
            }
            if (!Files.exists(filePath)) { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
            byte[] bytes = Files.readAllBytes(filePath);
            addCors(exchange);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    static class ProxyEnrollHandler implements HttpHandler {
        private final HttpClient client; private final String targetUrl;
        ProxyEnrollHandler(HttpClient client, String targetUrl) { this.client = client; this.targetUrl = targetUrl; }
        @Override public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1); exchange.close(); return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); exchange.close(); return;
            }
            byte[] reqBody = readAll(exchange.getRequestBody());
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(targetUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(reqBody))
                        .build();
                HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(resp.statusCode(), resp.body() == null ? 0 : resp.body().length);
                try (OutputStream os = exchange.getResponseBody()) { if (resp.body() != null) os.write(resp.body()); }
            } catch (Exception e) {
                byte[] msg = ("Error de red: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(502, msg.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(msg); }
            }
        }
        private byte[] readAll(InputStream in) throws IOException { return in.readAllBytes(); }
    }
}