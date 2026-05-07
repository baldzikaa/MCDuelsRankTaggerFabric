package com.mcduelstagger.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class McDuelsClientTest {
    private HttpServer server;
    private URI baseUri;
    private final AtomicInteger hits = new AtomicInteger();

    @BeforeEach void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/public/player/", ex -> {
            hits.incrementAndGet();
            String name = ex.getRequestURI().getPath().substring("/public/player/".length());
            if (name.equals("Technoblade")) {
                byte[] body = ("{\"uuid\":\"b876ec32-e396-476b-a115-8438d83c67d4\",\"username\":\"Technoblade\"," +
                               "\"kits\":{\"crystal\":{\"rank\":\"high_dueler\",\"elo\":2488,\"position\":1}}}")
                              .getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, body.length);
                ex.getResponseBody().write(body);
            } else if (name.equals("boom")) {
                ex.sendResponseHeaders(500, -1);
            } else {
                byte[] body = "{\"error\":\"Player not found\"}".getBytes();
                ex.sendResponseHeaders(404, body.length);
                ex.getResponseBody().write(body);
            }
            ex.close();
        });
        server.start();
        baseUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach void stop() { server.stop(0); }

    @Test void twoHundredReturnsProfile() throws Exception {
        var client = new McDuelsClient(baseUri);
        Optional<Profile> p = client.fetchByUsername("Technoblade").get();
        assertTrue(p.isPresent());
        assertEquals("Technoblade", p.orElseThrow().username());
    }
    @Test void fourOhFourReturnsEmptyOptional() throws Exception {
        var client = new McDuelsClient(baseUri);
        assertTrue(client.fetchByUsername("nobody").get().isEmpty());
    }
    @Test void fiveHundredThrowsCompletion() {
        var client = new McDuelsClient(baseUri);
        var ex = assertThrows(java.util.concurrent.ExecutionException.class,
            () -> client.fetchByUsername("boom").get());
        assertInstanceOf(McDuelsClient.TransientApiException.class, ex.getCause());
    }
    @Test void usernameIsUrlEncoded() throws Exception {
        var client = new McDuelsClient(baseUri);
        client.fetchByUsername("name with spaces").get();
        assertTrue(hits.get() >= 1);
    }
}
