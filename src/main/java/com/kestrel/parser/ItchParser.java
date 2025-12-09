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

        while(true){
            
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

            //  Later we decode into event here
            // for now just print 
            if (messageType == 'A') {
                Event e = decodeAddOrder(body);
                System.out.println("ADD ORDER: id=" + e.orderId + " price=" + e.price + " qty=" + e.quantity);
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
    
        long price = rawPrice; // raw ITCH price, already integer (scaled)
    
        Event e = pool.get();
        byte side = (sideByte == 'B') ? (byte)1 : (byte)0;
        e.set(orderId, price, quantity, side, (byte)1);
    
        return e;
    }

        // Visible for testing
    public Event testDecodeAddOrder(byte[] body) {
        return decodeAddOrder(body);
    }

    
}
