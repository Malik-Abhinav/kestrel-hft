package com.kestrel.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisLiveUpdatePublisher implements LiveUpdatePublisher {

    private final JedisPool pool;
    private final String channel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisLiveUpdatePublisher(String host, int port, String channel) {
        this.pool = new JedisPool(host, port);
        this.channel = channel;
    }

    @Override
    public void publish(LiveUpdateEvent event) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, objectMapper.writeValueAsString(event));
        } catch (Exception ignored) {
            // Publishing is best effort so the drop path still completes without blocking on Redis.
        }
    }

    @Override
    public void close() {
        pool.close();
    }
}
