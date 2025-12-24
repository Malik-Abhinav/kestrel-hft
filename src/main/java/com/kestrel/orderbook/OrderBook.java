package com.kestrel.orderbook;

import com.kestrel.core.Event;

import java.util.Map;
import java.util.TreeMap;

public class OrderBook {

    private final TreeMap<Long, PriceLevel> bids =
            new TreeMap<>((a, b) -> Long.compare(b, a)); // DESC

    private final TreeMap<Long, PriceLevel> asks =
            new TreeMap<>(); // ASC

    private final int levelCapacity;

    public OrderBook() {
        this(1024);
    }

    public OrderBook(int levelCapacity) {
        this.levelCapacity = levelCapacity;
    }

    public void add(Event e) {
        TreeMap<Long, PriceLevel> side = (e.side == 1) ? bids : asks;

        PriceLevel level = side.get(e.price);
        if (level == null) {
            level = new PriceLevel(levelCapacity);
            side.put(e.price, level);
        }
        level.add(e);
    }

    public Event bestBid() {
        if (bids.isEmpty()) return null;
        Map.Entry<Long, PriceLevel> top = bids.firstEntry();
        return top.getValue().peek();
    }

    public Event bestAsk() {
        if (asks.isEmpty()) return null;
        Map.Entry<Long, PriceLevel> top = asks.firstEntry();
        return top.getValue().peek();
    }

    public long bestBidPrice() {
        return bids.isEmpty() ? -1 : bids.firstKey();
    }

    public long bestAskPrice() {
        return asks.isEmpty() ? -1 : asks.firstKey();
    }

    public Event popBestBid() {
        if (bids.isEmpty()) return null;
        Map.Entry<Long, PriceLevel> top = bids.firstEntry();
        PriceLevel level = top.getValue();
        Event e = level.poll();
        if (level.isEmpty()) {
            bids.pollFirstEntry();
        }
        return e;
    }

    public Event popBestAsk() {
        if (asks.isEmpty()) return null;
        Map.Entry<Long, PriceLevel> top = asks.firstEntry();
        PriceLevel level = top.getValue();
        Event e = level.poll();
        if (level.isEmpty()) {
            asks.pollFirstEntry();
        }
        return e;
    }
}
