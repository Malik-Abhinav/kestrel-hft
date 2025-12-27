package com.kestrel.engine;

import com.kestrel.buffer.RingBufferBus;
import com.kestrel.buffer.RingBufferFactory;
import com.kestrel.core.Event;
import com.kestrel.core.EventPool;
import com.kestrel.orderbook.OrderBook;
import com.kestrel.parser.ItchParser;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import java.util.concurrent.atomic.AtomicBoolean;

public class KestrelEngine {

private final RingBufferBus bus;
private final EventPool pool;
private final MatchingEngine matching;
private final AtomicBoolean running = new AtomicBoolean(false);

public KestrelEngine() {
    OneToOneRingBuffer ring = RingBufferFactory.create(1024 * 1024);

    this.bus = new RingBufferBus(ring);
    this.pool = new EventPool(1_000_000);
    this.matching = new MatchingEngine(new OrderBook());
}

public void start(String filePath) throws Exception {
    running.set(true);

    Thread consumer = new Thread(() -> {
        while (running.get()) {
            int n = bus.poll((msgType, side, orderId, price, qty) -> {
                Event e = pool.get();
                e.set(orderId, price, qty, side, (byte)1);
                matching.onAddOrder(e);
            }, 4096);

            if (n == 0) Thread.onSpinWait();
        }
    }, "kestrel-consumer");

    consumer.start();

    // parser writes into bus
    ItchParser parser = new ItchParser(pool) {
        @Override
        protected void onAddOrder(byte side, long orderId, long price, int qty) {
            while (!bus.publishAdd(side, orderId, price, qty)) {
                Thread.onSpinWait();
            }
        }
    };

    parser.parse(filePath);

    running.set(false);
    consumer.join();
}

public static void main(String[] args) throws Exception {
    System.out.println("Kestrel Engine Booting...");
    KestrelEngine ke = new KestrelEngine();

    // placeholder for now
    System.out.println("Engine initialized.");
}


}