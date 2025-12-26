package com.kestrel.buffer;

import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import java.nio.ByteBuffer;

public class RingBufferBus {

    public static final int MSG_TYPE_ADD = 1;
    public static final int RECORD_TYPE_EVENT = 1; // ring buffer message type id

    // 24 bytes payload
    public static final int EVENT_SIZE = 24;

    private final OneToOneRingBuffer ring;

    public RingBufferBus(OneToOneRingBuffer ring) {
        this.ring = ring;
    }

    public boolean publishAdd(byte side, long orderId, long price, int qty) {
        UnsafeBuffer tmp = new UnsafeBuffer(ByteBuffer.allocateDirect(EVENT_SIZE));

        tmp.putByte(0, (byte) MSG_TYPE_ADD);
        tmp.putByte(1, side);
        tmp.putShort(2, (short) 0);
        tmp.putLong(4, orderId);
        tmp.putLong(12, price);
        tmp.putInt(20, qty);

        return ring.write(RECORD_TYPE_EVENT, tmp, 0, EVENT_SIZE);
    }

    public int poll(EventHandler handler, int limit) {
        return ring.read((msgTypeId, buffer, index, length) -> {
            if (msgTypeId != RECORD_TYPE_EVENT) return;

            byte msgType = buffer.getByte(index);
            byte side = buffer.getByte(index + 1);
            long orderId = buffer.getLong(index + 4);
            long price = buffer.getLong(index + 12);
            int qty = buffer.getInt(index + 20);

            handler.onEvent(msgType, side, orderId, price, qty);
        }, limit);
    }

    public interface EventHandler {
        void onEvent(byte msgType, byte side, long orderId, long price, int qty);
    }
}
