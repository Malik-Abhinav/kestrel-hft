package com.kestrel.orderbook;

import com.kestrel.core.Event;

public class PriceLevel {

    private final Event[] orders;
    private int head = 0;
    private int tail = 0;

    public PriceLevel(int capacity) {
        this.orders = new Event[capacity];
    }

    public void add(Event e) {
        orders[tail++] = e;
    }

    public Event peek() {
        return head < tail ? orders[head] : null;
    }

    public Event poll() {
        if (head >= tail) return null;
        Event e = orders[head];
        orders[head] = null; // help avoid retaining references
        head++;
        return e;
    }

    public boolean isEmpty() {
        return head >= tail;
    }
}
