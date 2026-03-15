package com.kestrel.api;

import io.javalin.Javalin;

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

        app.get("/api/drop/state", ctx -> ctx.json(dropStateService.currentState()));
        app.post("/api/drop/start", ctx -> {
            StartDropRequest request = ctx.body().isBlank()
                    ? null
                    : ctx.bodyAsClass(StartDropRequest.class);
            DropState state = dropStateService.startDrop(request);
            ctx.status(201).json(state);
        });
        liveUpdateDispatcher.registerWebSocket(app);

        return app;
    }
}
