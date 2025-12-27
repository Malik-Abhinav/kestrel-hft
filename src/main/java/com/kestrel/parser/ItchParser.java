package com.kestrel.parser;

import com.kestrel.core.Event;
import com.kestrel.core.EventPool;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ItchParser {

private final EventPool pool;

public ItchParser(EventPool pool) {
    this.pool = pool;
}

public void parse(String filePath) throws IOException {
    FileInputStream fis = new FileInputStream(filePath);

    byte[] header = new byte[2]; // length + message type

    while (true) {
        int read = fis.read(header);
        if (read == -1) {
            break;
        }

        int length = Byte.toUnsignedInt(header[0]);
        byte messageType = header[1];

        byte[] body = new byte[length - 2];
        int n = fis.read(body);
        if (n < body.length) {
            break;
        }

        if (messageType == 'A') {
            // decode and trigger callback
            decodeAddOrder(body);
        }
    }

    fis.close();
}

private Event decodeAddOrder(byte[] body) {
    ByteBuffer buffer = ByteBuffer.wrap(body);
    buffer.order(ByteOrder.BIG_ENDIAN);

    long orderId = buffer.getLong();
    byte sideByte = buffer.get();
    int quantity = buffer.getInt();
    buffer.position(buffer.position() + 8); // skip stock 8 bytes
    int rawPrice = buffer.getInt();

    long price = rawPrice;
    byte side = (sideByte == 'B') ? (byte)1 : (byte)0;

    // fire callback (used by KestrelEngine override)
    onAddOrder(side, orderId, price, quantity);

    // still build Event for tests or alternate usage
    Event e = pool.get();
    e.set(orderId, price, quantity, side, (byte)1);
    return e;
}

// Visible for testing
public Event testDecodeAddOrder(byte[] body) {
    return decodeAddOrder(body);
}

protected void onAddOrder(byte side, long orderId, long price, int qty) {
    // default: print. KestrelEngine will override this to publish into ring buffer.
    System.out.println("ADD ORDER id=" + orderId + " price=" + price + " qty=" + qty);
}


}