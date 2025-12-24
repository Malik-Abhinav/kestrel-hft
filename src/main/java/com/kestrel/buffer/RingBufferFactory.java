package com.kestrel.buffer;

import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import java.nio.ByteBuffer;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;

public class RingBufferFactory {

    public static OneToOneRingBuffer create(int capacity) {
        // capacity must be power of two plus trailer length
        int total = capacity + TRAILER_LENGTH;
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(total));
        return new OneToOneRingBuffer(buffer);
    }
}
