package com.kestrel;

import com.kestrel.api.DropStateService;
import com.kestrel.api.KestrelApiServer;
import com.kestrel.api.LiveUpdateDispatcher;
import com.kestrel.api.LiveUpdateEvent;
import com.kestrel.reservation.ReservationResult;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LiveUpdateWebSocketTest {

    @Test
    public void websocketReceivesReservationEventsDuringDrop() throws Exception {
        LiveUpdateDispatcher dispatcher = LiveUpdateDispatcher.localOnly();
        DropStateService dropStateService = new DropStateService(result ->
                dispatcher.publish(LiveUpdateEvent.from(result))
        );
        Javalin app = KestrelApiServer.createApp(dropStateService, dispatcher);
        app.start(0);

        try {
            CompletableFuture<String> firstMessage = new CompletableFuture<>();
            WebSocket webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(
                            URI.create("ws://localhost:" + app.port() + "/ws/live-updates"),
                            new SingleMessageListener(firstMessage)
                    )
                    .join();

            String body = """
                    {
                      "seats":[{"seatId":"A1","priceCents":15000}],
                      "requests":[{"requestId":1,"sequence":1,"userId":"User_1","seatId":"A1"}]
                    }
                    """;

            HttpResponse<String> response = sendStartRequest(app, body);
            assertEquals(201, response.statusCode());

            String eventPayload = firstMessage.get(5, TimeUnit.SECONDS);
            assertTrue(eventPayload.contains("\"seatId\":\"A1\""));
            assertTrue(eventPayload.contains("\"status\":\"SOLD\""));
            assertTrue(eventPayload.contains("\"userId\":\"User_1\""));
            assertTrue(eventPayload.contains("\"sequence\":1"));

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
        } finally {
            app.stop();
            dispatcher.close();
        }
    }

    private HttpResponse<String> sendStartRequest(Javalin app, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/api/drop/start"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static final class SingleMessageListener implements WebSocket.Listener {
        private final CompletableFuture<String> messageFuture;
        private final StringBuilder buffer = new StringBuilder();

        private SingleMessageListener(CompletableFuture<String> messageFuture) {
            this.messageFuture = messageFuture;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last && !messageFuture.isDone()) {
                messageFuture.complete(buffer.toString());
            }
            webSocket.request(1);
            return null;
        }
    }
}
