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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class McDuelsClient implements AutoCloseable {
    public static final URI DEFAULT_BASE = URI.create("https://mcduels.com");

    private final URI base;
    private final HttpClient http;
    private final ExecutorService exec;
    private final Gson gson = new Gson();

    public McDuelsClient() { this(DEFAULT_BASE); }

    public McDuelsClient(URI base) {
        this.base = base;
        this.exec = Executors.newFixedThreadPool(8, r -> {
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
        if (username == null || username.isBlank()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
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
        // 4xx (other than 404) is a request-side problem that won't fix itself on retry.
        // 5xx and unknown codes are server-side and may recover.
        if (code >= 400 && code < 500) {
            throw new PermanentApiException("HTTP " + code);
        }
        throw new TransientApiException("HTTP " + code);
    }

    @Override public void close() {
        exec.shutdown();
        try {
            if (!exec.awaitTermination(2, TimeUnit.SECONDS)) exec.shutdownNow();
        } catch (InterruptedException ie) {
            exec.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** Server-side or network error — worth retrying with backoff. */
    public static final class TransientApiException extends RuntimeException {
        public TransientApiException(String msg) { super(msg); }
        public TransientApiException(String msg, Throwable cause) { super(msg, cause); }
    }
    /** 4xx response — request is bad and will not succeed on retry. */
    public static final class PermanentApiException extends RuntimeException {
        public PermanentApiException(String msg) { super(msg); }
    }
}
