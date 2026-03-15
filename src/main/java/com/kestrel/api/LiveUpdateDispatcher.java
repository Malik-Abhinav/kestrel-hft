package com.kestrel.api;

import io.javalin.Javalin;

public class LiveUpdateDispatcher implements AutoCloseable {

    private final LiveUpdateBroadcaster broadcaster;
    private final LiveUpdatePublisher publisher;
    private final RedisLiveUpdateSubscriber redisSubscriber;

    private LiveUpdateDispatcher(
            LiveUpdateBroadcaster broadcaster,
            LiveUpdatePublisher publisher,
            RedisLiveUpdateSubscriber redisSubscriber
    ) {
        this.broadcaster = broadcaster;
        this.publisher = publisher;
        this.redisSubscriber = redisSubscriber;
    }

    public static LiveUpdateDispatcher fromEnv() {
        LiveUpdateBroadcaster broadcaster = new LiveUpdateBroadcaster();
        if (!redisEnabled()) {
            return new LiveUpdateDispatcher(broadcaster, broadcaster::broadcast, null);
        }

        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String channel = System.getenv().getOrDefault("REDIS_CHANNEL", "kestrel:live-updates");

        RedisLiveUpdatePublisher publisher = new RedisLiveUpdatePublisher(host, port, channel);
        RedisLiveUpdateSubscriber subscriber = new RedisLiveUpdateSubscriber(host, port, channel, broadcaster::broadcast);
        subscriber.start();
        return new LiveUpdateDispatcher(broadcaster, publisher, subscriber);
    }

    public static LiveUpdateDispatcher localOnly() {
        LiveUpdateBroadcaster broadcaster = new LiveUpdateBroadcaster();
        return new LiveUpdateDispatcher(broadcaster, broadcaster::broadcast, null);
    }

    public void publish(LiveUpdateEvent event) {
        publisher.publish(event);
    }

    public void registerWebSocket(Javalin app) {
        app.ws("/ws/live-updates", broadcaster::configure);
    }

    @Override
    public void close() {
        publisher.close();
        if (redisSubscriber != null) {
            redisSubscriber.close();
        }
    }

    private static boolean redisEnabled() {
        return "true".equalsIgnoreCase(System.getenv("REDIS_ENABLED"));
    }
}
