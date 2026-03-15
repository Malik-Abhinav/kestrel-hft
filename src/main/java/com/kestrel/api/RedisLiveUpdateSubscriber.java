package com.kestrel.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public class RedisLiveUpdateSubscriber implements AutoCloseable {

    private final JedisPool pool;
    private final String channel;
    private final Consumer<LiveUpdateEvent> eventConsumer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Thread worker;
    private JedisPubSub pubSub;

    public RedisLiveUpdateSubscriber(String host, int port, String channel, Consumer<LiveUpdateEvent> eventConsumer) {
        this.pool = new JedisPool(host, port);
        this.channel = channel;
        this.eventConsumer = eventConsumer;
    }

    public void start() {
        worker = new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String receivedChannel, String message) {
                        if (!channel.equals(receivedChannel)) {
                            return;
                        }
                        try {
                            eventConsumer.accept(objectMapper.readValue(message, LiveUpdateEvent.class));
                        } catch (Exception ignored) {
                            // Ignore malformed live events instead of breaking the subscriber thread.
                        }
                    }
                };
                jedis.subscribe(pubSub, channel);
            } catch (Exception ignored) {
                // If Redis is unavailable, the API still runs; live updates simply won't bridge through Redis.
            }
        }, "live-update-subscriber");
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void close() {
        if (pubSub != null) {
            pubSub.unsubscribe();
        }
        if (worker != null) {
            try {
                worker.join(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        pool.close();
    }
}
