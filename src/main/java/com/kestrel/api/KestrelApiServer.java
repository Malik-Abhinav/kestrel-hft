package com.kestrel.api;

import io.javalin.Javalin;

public class KestrelApiServer {

    public static void main(String[] args) {
        DropStateService dropStateService = new DropStateService();
        Javalin app = createApp(dropStateService);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        app.start(port);
    }

    public static Javalin createApp(DropStateService dropStateService) {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false);

        app.get("/api/drop/state", ctx -> ctx.json(dropStateService.currentState()));
        app.post("/api/drop/start", ctx -> {
            StartDropRequest request = ctx.body().isBlank()
                    ? null
                    : ctx.bodyAsClass(StartDropRequest.class);
            DropState state = dropStateService.startDrop(request);
            ctx.status(201).json(state);
        });

        return app;
    }
}
