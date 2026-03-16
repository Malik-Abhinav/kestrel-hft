package com.kestrel.api;

import io.javalin.Javalin;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class KestrelApiServer {

    public static void main(String[] args) {
        LiveUpdateDispatcher liveUpdateDispatcher = LiveUpdateDispatcher.fromEnv();
        DropStateService dropStateService = new DropStateService(result ->
                liveUpdateDispatcher.publish(LiveUpdateEvent.from(result))
        );
        Javalin app = createApp(dropStateService, liveUpdateDispatcher);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        Runtime.getRuntime().addShutdownHook(new Thread(liveUpdateDispatcher::close, "live-update-shutdown"));
        app.start(port);
    }

    public static Javalin createApp(DropStateService dropStateService) {
        return createApp(dropStateService, LiveUpdateDispatcher.localOnly());
    }

    public static Javalin createApp(DropStateService dropStateService, LiveUpdateDispatcher liveUpdateDispatcher) {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false);

        app.get("/", ctx -> ctx.html(loadResource("public/index.html")));
        app.get("/api/drop/state", ctx -> ctx.json(dropStateService.currentState()));
        app.post("/api/drop/start", ctx -> {
            StartDropRequest request = ctx.body().isBlank()
                    ? null
                    : ctx.bodyAsClass(StartDropRequest.class);
            DropState state = dropStateService.startDrop(request);
            ctx.status(201).json(state);
        });
        app.post("/api/drop/replay", ctx -> {
            try {
                DropState state = dropStateService.replayLastDrop();
                ctx.status(201).json(state);
            } catch (IllegalStateException e) {
                ctx.status(409).json(java.util.Map.of("error", e.getMessage()));
            }
        });
        liveUpdateDispatcher.registerWebSocket(app);

        return app;
    }

    private static String loadResource(String path) {
        try (InputStream inputStream = KestrelApiServer.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load resource: " + path, e);
        }
    }
}
