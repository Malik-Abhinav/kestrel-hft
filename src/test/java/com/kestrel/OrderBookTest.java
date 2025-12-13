package com.kestrel;

import com.kestrel.core.Event;
import com.kestrel.core.EventPool;
import com.kestrel.orderbook.OrderBook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {

    @Test
    public void testBestBidAndAsk() {
        EventPool pool = new EventPool(10);
        OrderBook book = new OrderBook();

        Event bid = pool.get();
        bid.set(1, 100, 10, (byte)1, (byte)1);

        Event ask = pool.get();
        ask.set(2, 105, 10, (byte)0, (byte)1);

        book.add(bid);
        book.add(ask);

        assertEquals(100, book.bestBid().price);
        assertEquals(105, book.bestAsk().price);
    }
}
