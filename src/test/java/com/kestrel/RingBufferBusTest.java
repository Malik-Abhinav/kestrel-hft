package com.kestrel;

import com.kestrel.buffer.RingBufferBus;
import com.kestrel.buffer.RingBufferFactory;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RingBufferBusTest {

    @Test
    public void publishAndConsume() {
        OneToOneRingBuffer ring = RingBufferFactory.create(1024 * 64);
        RingBufferBus bus = new RingBufferBus(ring);

        assertTrue(bus.publishAdd((byte)1, 1L, 100L, 10));

        final int[] seen = {0};

        bus.poll((msgType, side, orderId, price, qty) -> {
            assertEquals(RingBufferBus.MSG_TYPE_ADD, msgType);
            assertEquals(1, side);
            assertEquals(1L, orderId);
            assertEquals(100L, price);
            assertEquals(10, qty);
            seen[0]++;
        }, 10);

        assertEquals(1, seen[0]);
    }
}
