package frontend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

final class ApiClient {
    private static final String BASE_URL = "http://127.0.0.1:8080/api";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    CompletableFuture<JsonNode> get(String path) {
        return send("GET", path, null);
    }

    CompletableFuture<JsonNode> post(String path) {
        return send("POST", path, "");
    }

    CompletableFuture<JsonNode> postForm(String path, String... fields) {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < fields.length; i += 2) {
            if (i > 0) body.append('&');
            body.append(encode(fields[i])).append('=').append(encode(fields[i + 1]));
        }
        return send("POST", path, body.toString());
    }

    private CompletableFuture<JsonNode> send(String method, String path, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json");

        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else {
            builder.GET();
        }

        return http.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenCompose(response -> {
                    try {
                        JsonNode json = mapper.readTree(response.body());
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            return CompletableFuture.completedFuture(json);
                        }
                        String message = json.has("erro") ? json.get("erro").asText() : "Erro na operação.";
                        return CompletableFuture.failedFuture(new ApiException(message));
                    } catch (IOException e) {
                        return CompletableFuture.failedFuture(new ApiException("Resposta inválida do backend."));
                    }
                });
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    static final class ApiException extends RuntimeException {
        ApiException(String message) {
            super(message);
        }
    }
}
