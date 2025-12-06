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
            System.out.println("MSG TYPE: " + (char)messageType + " LEN: " + length);
        }
        fis.close();
    }
}
