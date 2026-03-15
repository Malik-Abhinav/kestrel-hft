package com.kestrel.api;

public interface LiveUpdatePublisher extends AutoCloseable {

    void publish(LiveUpdateEvent event);

    @Override
    default void close() {
    }
}
