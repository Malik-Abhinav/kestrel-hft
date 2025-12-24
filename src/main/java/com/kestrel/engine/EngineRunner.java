package com.kestrel.engine;

import com.kestrel.buffer.RingBufferBus;
import com.kestrel.buffer.RingBufferFactory;
import com.kestrel.core.Event;
import com.kestrel.core.EventPool;
import com.kestrel.orderbook.OrderBook;
import com.kestrel.parser.ItchParser;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import java.util.concurrent.atomic.AtomicBoolean;

public class EngineRunner {

    public static void main(String[] args) throws Exception {
        OneToOneRingBuffer ring = RingBufferFactory.create(1024 * 1024);

        RingBufferBus bus = new RingBufferBus(ring);

        EventPool pool = new EventPool(1_000_000);
        OrderBook book = new OrderBook();
        MatchingEngine engine = new MatchingEngine(book);

        AtomicBoolean running = new AtomicBoolean(true);

        // Consumer thread: matching engine
        Thread consumer = new Thread(() -> {
            while (running.get()) {
                int n = bus.poll((msgType, side, orderId, price, qty) -> {
                    if (msgType == RingBufferBus.MSG_TYPE_ADD) {
                        Event e = pool.get();
                        e.set(orderId, price, qty, side, (byte)1);
                        engine.onAddOrder(e);
                    }
                }, 1000);

                if (n == 0) {
                    Thread.onSpinWait();
                }
            }
        }, "engine-consumer");

        consumer.start();

        // Producer: parse file and publish events
        // For now, we bypass real file parsing and publish a few events to prove threading works.
        for (int i = 0; i < 10000; i++) {
            bus.publishAdd((byte)1, i, 100 + (i % 5), 10);
            bus.publishAdd((byte)0, 10_000 + i, 100 + (i % 5), 10);
        }

        Thread.sleep(500);
        running.set(false);
        consumer.join();

        System.out.println("EngineRunner done.");
    }
}
