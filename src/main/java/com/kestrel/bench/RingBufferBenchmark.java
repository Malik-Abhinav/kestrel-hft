package com.kestrel.bench;

import com.kestrel.buffer.RingBufferBus;
import com.kestrel.buffer.RingBufferFactory;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

public class RingBufferBenchmark {

public static void main(String[] args) throws Exception {
    OneToOneRingBuffer ring = RingBufferFactory.create(1024 * 1024);
    RingBufferBus bus = new RingBufferBus(ring);

    final int total = 1_000_000;
    final int[] consumed = {0};

    Thread consumer = new Thread(() -> {
        while (consumed[0] < total) {
            int n = bus.poll((msgType, side, orderId, price, qty) -> {
                consumed[0]++;
            }, 1024);

            if (n == 0) {
                Thread.onSpinWait();
            }
        }
    }, "bench-consumer");

    consumer.start();

    long start = System.nanoTime();

    for (int i = 0; i < total; i++) {
        while (!bus.publishAdd((byte) 1, i, 100L, 10)) {
            Thread.onSpinWait();
        }
    }

    consumer.join();

    long end = System.nanoTime();
    double seconds = (end - start) / 1_000_000_000.0;
    double throughput = total / seconds;

    System.out.println("Events: " + total);
    System.out.println("Seconds: " + seconds);
    System.out.println("Throughput: " + throughput + " events/sec");
}


}