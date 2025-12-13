package com.kestrel.orderbook;

import com.kestrel.core.Event;

import java.util.TreeMap;


public class OrderBook {


    private final TreeMap<Long, PriceLevel> bids = 
            new TreeMap<>((a, b) -> Long.compare(b, a)); //desc

    private final TreeMap<Long, PriceLevel> asks = 
            new TreeMap<>(); // ASC

    public void add(Event e){
        TreeMap<Long, PriceLevel> side =   
                e.side == 1 ? bids : asks;

        PriceLevel level = side.get(e.price);
        if (level == null){
            level = new PriceLevel(1024);
            side.put(e.price, level);
        }

        level.add(e);
    }


    public Event bestBid(){
        if (bids.isEmpty()) return null;
        return bids.firstEntry().getValue().peek();
    }

    public Event bestAsk(){
        if (asks.isEmpty()) return null;
        return asks.firstEntry().getValue().peek();
    }


}
