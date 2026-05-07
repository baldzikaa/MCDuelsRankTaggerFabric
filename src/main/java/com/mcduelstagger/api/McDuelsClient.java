package com.mcduelstagger.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class McDuelsClient {
    public static final URI DEFAULT_BASE = URI.create("https://mcduels.com");

    private final URI base;
    private final HttpClient http;
    private final Gson gson = new Gson();

    public McDuelsClient() { this(DEFAULT_BASE); }

    public McDuelsClient(URI base) {
        this.base = base;
        Executor exec = Executors.newFixedThreadPool(8, r -> {
            Thread t = new Thread(r, "mcduelstagger-http");
            t.setDaemon(true);
            return t;
        });
        this.http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .executor(exec)
            .build();
    }

    public CompletableFuture<Optional<Profile>> fetchByUsername(String username) {
        String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8);
        URI uri = base.resolve("/public/player/" + encoded);
        HttpRequest req = HttpRequest.newBuilder(uri)
            .GET()
            .timeout(Duration.ofSeconds(8))
            .header("User-Agent", "mcduels-tagger/0.1.0")
            .header("Accept", "application/json")
            .build();
        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .thenApply(this::handle);
    }

    private Optional<Profile> handle(HttpResponse<String> resp) {
        int code = resp.statusCode();
        if (code == 200) {
            try { return Optional.of(gson.fromJson(resp.body(), Profile.class)); }
            catch (JsonSyntaxException e) { throw new TransientApiException("malformed JSON", e); }
        }
        if (code == 404) return Optional.empty();
        throw new TransientApiException("HTTP " + code);
    }

    public static final class TransientApiException extends RuntimeException {
        public TransientApiException(String msg) { super(msg); }
        public TransientApiException(String msg, Throwable cause) { super(msg, cause); }
    }
}
