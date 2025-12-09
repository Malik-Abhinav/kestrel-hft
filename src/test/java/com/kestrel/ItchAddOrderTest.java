package com.kestrel;

import com.kestrel.core.Event;
import com.kestrel.core.EventPool;
import com.kestrel.parser.ItchParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Assertions.*;



public class ItchAddOrderTest {


    @Test
    public void testDecodeAddOrder() throws Exception {
        EventPool pool = new EventPool(10);
        ItchParser parser = new ItchParser(pool);

        // Build a fake 'A' message body

        ByteBuffer buf = ByteBuffer.allocate(1 + 8 + 1 + 4 + 8 + 4);
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.put((byte)'A');         // message type
        buf.put((byte)0x00);        // ignore, parser reads header seperartely
        buf.clear();


        // Actual body (parser receives  body only)
        ByteBuffer body = ByteBuffer.allocate(8 + 1 + 4 + 8 + 4);
        body.order(ByteOrder.BIG_ENDIAN);

        body.putLong(12345L);       //order id
        body.put((byte)'B');            //buy
        body.putInt(100);       // quantity
        body.putLong(0L);       // stock (ignored)
        body.putInt(250000);        // price (e.g , 25.0000)

        Event e = parser.testDecodeAddOrder(body.array());

        assertEquals(12345L, e.orderId);
        assertEquals(100, e.quantity);
        assertEquals(250000, e.price);
        assertEquals(1, e.side);        //buy 

    }

}
