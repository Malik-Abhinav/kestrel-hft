package com.kestrel;

import com.kestrel.api.DropStateService;
import com.kestrel.api.KestrelApiServer;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DropApiServerTest {

    @Test
    public void stateEndpointReturnsIdleBeforeAnyDropStarts() throws Exception {
        withServer(app -> {
            HttpResponse<String> response = sendRequest(app, "GET", "/api/drop/state", null);
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("\"status\":\"IDLE\""));
            assertTrue(response.body().contains("\"processedCount\":0"));
            assertTrue(response.body().contains("\"replayAvailable\":false"));
        });
    }

    @Test
    public void rootEndpointServesDashboard() throws Exception {
        withServer(app -> {
            HttpResponse<String> response = sendRequest(app, "GET", "/", null);
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Seat Drop Dashboard"));
            assertTrue(response.body().contains("Start Drop"));
            assertTrue(response.body().contains("/ws/live-updates"));
        });
    }

    @Test
    public void startEndpointRunsDropAndStateEndpointReflectsResults() throws Exception {
        withServer(app -> {
            String body = """
                    {
                      "seats":[{"seatId":"A1","priceCents":15000}],
                      "requests":[
                        {"requestId":1,"sequence":1,"userId":"User_1","seatId":"A1"},
                        {"requestId":2,"sequence":2,"userId":"User_2","seatId":"A1"}
                      ]
                    }
                    """;

            HttpResponse<String> startResponse = sendRequest(app, "POST", "/api/drop/start", body);
            assertEquals(201, startResponse.statusCode());
            assertTrue(startResponse.body().contains("\"status\":\"COMPLETED\""));
            assertTrue(startResponse.body().contains("\"processedCount\":2"));
            assertTrue(startResponse.body().contains("\"soldCount\":1"));
            assertTrue(startResponse.body().contains("\"rejectedCount\":1"));
            assertTrue(startResponse.body().contains("\"averageLatencyMs\":"));
            assertTrue(startResponse.body().contains("\"p95LatencyMs\":"));

            HttpResponse<String> stateResponse = sendRequest(app, "GET", "/api/drop/state", null);
            assertEquals(200, stateResponse.statusCode());
            assertTrue(stateResponse.body().contains("\"dropId\":\"drop-1\""));
            assertTrue(stateResponse.body().contains("\"availableSeats\":0"));
            assertTrue(stateResponse.body().contains("\"seatId\":\"A1\""));
            assertTrue(stateResponse.body().contains("\"replayAvailable\":true"));
        });
    }

    @Test
    public void replayEndpointReproducesWinnersAndOrdering() throws Exception {
        withServer(app -> {
            String body = """
                    {
                      "seats":[
                        {"seatId":"A1","priceCents":15000},
                        {"seatId":"A2","priceCents":15000}
                      ],
                      "requests":[
                        {"requestId":1,"sequence":1,"userId":"User_1","seatId":"A1"},
                        {"requestId":2,"sequence":2,"userId":"User_2","seatId":"A1"},
                        {"requestId":3,"sequence":3,"userId":"User_3","seatId":"A2"}
                      ]
                    }
                    """;

            HttpResponse<String> startResponse = sendRequest(app, "POST", "/api/drop/start", body);
            assertEquals(201, startResponse.statusCode());

            HttpResponse<String> replayResponse = sendRequest(app, "POST", "/api/drop/replay", "");
            assertEquals(201, replayResponse.statusCode());
            assertTrue(replayResponse.body().contains("\"dropId\":\"drop-2\""));
            assertTrue(replayResponse.body().contains("\"replayOfDropId\":\"drop-1\""));
            assertTrue(replayResponse.body().contains("\"sequence\":1"));
            assertTrue(replayResponse.body().contains("\"sequence\":2"));
            assertTrue(replayResponse.body().contains("\"sequence\":3"));
            assertTrue(replayResponse.body().contains("\"reservedByUserId\":\"User_1\""));
            assertTrue(replayResponse.body().contains("\"reservedByUserId\":\"User_3\""));
            assertTrue(replayResponse.body().contains("\"status\":\"REJECTED\""));
        });
    }

    @Test
    public void replayEndpointReturnsConflictBeforeAnyDropStarts() throws Exception {
        withServer(app -> {
            HttpResponse<String> replayResponse = sendRequest(app, "POST", "/api/drop/replay", "");
            assertEquals(409, replayResponse.statusCode());
            assertTrue(replayResponse.body().contains("No completed drop is available to replay"));
        });
    }

    private void withServer(ServerAssertion assertion) throws Exception {
        Javalin app = KestrelApiServer.createApp(new DropStateService());
        app.start(0);
        try {
            assertion.run(app);
        } finally {
            app.stop();
        }
    }

    private HttpResponse<String> sendRequest(Javalin app, String method, String path, String body)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path));

        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else {
            builder.GET();
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @FunctionalInterface
    private interface ServerAssertion {
        void run(Javalin app) throws Exception;
    }
}
