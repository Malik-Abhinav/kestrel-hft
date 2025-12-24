package com.kestrel;

import com.kestrel.core.Event;
import com.kestrel.core.EventPool;
import com.kestrel.engine.MatchingEngine;
import com.kestrel.engine.Trade;
import com.kestrel.orderbook.OrderBook;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchingEngineTest {

    @Test
    public void testBuyMatchesExistingAskFullFill() {
        EventPool pool = new EventPool(10);
        OrderBook book = new OrderBook();
        MatchingEngine engine = new MatchingEngine(book);

        Event ask = pool.get();
        ask.set(200, 105, 10, (byte)0, (byte)1);
        book.add(ask);

        Event buy = pool.get();
        buy.set(100, 110, 10, (byte)1, (byte)1);

        List<Trade> trades = engine.onAddOrder(buy);

        assertEquals(1, trades.size());
        Trade t = trades.get(0);
        assertEquals(100, t.takerOrderId);
        assertEquals(200, t.makerOrderId);
        assertEquals(105, t.price);
        assertEquals(10, t.quantity);

        assertNull(book.bestAsk());
        assertNull(book.bestBid());
    }

    @Test
    public void testBuyPartialFillLeavesRemainderOnBook() {
        EventPool pool = new EventPool(10);
        OrderBook book = new OrderBook();
        MatchingEngine engine = new MatchingEngine(book);

        Event ask = pool.get();
        ask.set(200, 105, 5, (byte)0, (byte)1);
        book.add(ask);

        Event buy = pool.get();
        buy.set(100, 110, 10, (byte)1, (byte)1);

        List<Trade> trades = engine.onAddOrder(buy);

        assertEquals(1, trades.size());
        assertEquals(5, trades.get(0).quantity);

        assertNull(book.bestAsk());
        assertNotNull(book.bestBid());
        assertEquals(110, book.bestBid().price);
        assertEquals(5, book.bestBid().quantity);
        assertEquals(100, book.bestBid().orderId);
    }

    @Test
    public void testNonCrossingOrderGoesToBook() {
        EventPool pool = new EventPool(10);
        OrderBook book = new OrderBook();
        MatchingEngine engine = new MatchingEngine(book);

        Event buy = pool.get();
        buy.set(100, 100, 10, (byte)1, (byte)1);

        List<Trade> trades = engine.onAddOrder(buy);

        assertEquals(0, trades.size());
        assertNotNull(book.bestBid());
        assertEquals(100, book.bestBid().price);
        assertEquals(10, book.bestBid().quantity);
    }
}
