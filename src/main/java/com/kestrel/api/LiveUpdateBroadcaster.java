package com.kestrel.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LiveUpdateBroadcaster {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WsContext> sessions = ConcurrentHashMap.newKeySet();

    public void configure(WsConfig ws) {
        ws.onConnect(ctx -> sessions.add(ctx));
        ws.onClose(ctx -> sessions.remove(ctx));
        ws.onError(ctx -> sessions.remove(ctx));
    }

    public void broadcast(LiveUpdateEvent event) {
        String payload = serialize(event);
        for (WsContext session : sessions) {
            session.send(payload);
        }
    }

    private String serialize(LiveUpdateEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize live update event", e);
        }
    }
}
