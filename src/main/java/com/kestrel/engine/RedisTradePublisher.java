package com.kestrel.engine;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class RedisTradePublisher implements AutoCloseable {

    private final JedisPool pool;
    private final String channel;
    private final boolean enabled;

    private RedisTradePublisher(JedisPool pool, String channel, boolean enabled) {
        this.pool = pool;
        this.channel = channel;
        this.enabled = enabled;
    }

    public static RedisTradePublisher fromEnv() {
        String enabledEnv = System.getenv("REDIS_ENABLED");
        if (!"true".equalsIgnoreCase(enabledEnv)) {
            return new RedisTradePublisher(null, "", false);
        }

        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        String portEnv = System.getenv().getOrDefault("REDIS_PORT", "6379");
        String channel = System.getenv().getOrDefault("REDIS_CHANNEL", "kestrel:trades");

        int port = Integer.parseInt(portEnv);
        JedisPool pool = new JedisPool(host, port);
        return new RedisTradePublisher(pool, channel, true);
    }

    public void publishTrade(Trade trade) {
        if (!enabled) {
            return;
        }

        String payload = toJson(trade);
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, payload);
        } catch (Exception ignored) {
            // best-effort publishing; avoid impacting matching latency
        }
    }

    private String toJson(Trade trade) {
        return "{\"takerOrderId\":" + trade.takerOrderId +
                ",\"makerOrderId\":" + trade.makerOrderId +
                ",\"price\":" + trade.price +
                ",\"quantity\":" + trade.quantity + "}";
    }

    @Override
    public void close() {
        if (pool != null) {
            pool.close();
        }
    }
}
