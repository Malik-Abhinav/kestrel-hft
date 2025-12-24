package com.kestrel.engine;

import com.kestrel.core.Event;
import com.kestrel.orderbook.OrderBook;

import java.util.ArrayList;
import java.util.List;

public class MatchingEngine {

    private final OrderBook book;

    public MatchingEngine(OrderBook book) {
        this.book = book;
    }

    public List<Trade> onAddOrder(Event incoming) {
        if (incoming.side == 1) {
            return matchBuy(incoming);
        } else {
            return matchSell(incoming);
        }
    }

    private List<Trade> matchBuy(Event buy) {
        List<Trade> trades = new ArrayList<>();

        while (buy.quantity > 0) {
            long bestAskPrice = book.bestAskPrice();
            if (bestAskPrice < 0) break;
            if (buy.price < bestAskPrice) break;

            Event bestAsk = book.bestAsk();
            if (bestAsk == null) break;

            int fillQty = Math.min(buy.quantity, bestAsk.quantity);

            trades.add(new Trade(
                    buy.orderId,
                    bestAsk.orderId,
                    bestAskPrice,
                    fillQty
            ));

            buy.quantity -= fillQty;
            bestAsk.quantity -= fillQty;

            if (bestAsk.quantity == 0) {
                book.popBestAsk();
            }
        }

        if (buy.quantity > 0) {
            book.add(buy);
        }

        return trades;
    }

    private List<Trade> matchSell(Event sell) {
        List<Trade> trades = new ArrayList<>();

        while (sell.quantity > 0) {
            long bestBidPrice = book.bestBidPrice();
            if (bestBidPrice < 0) break;
            if (sell.price > bestBidPrice) break;

            Event bestBid = book.bestBid();
            if (bestBid == null) break;

            int fillQty = Math.min(sell.quantity, bestBid.quantity);

            trades.add(new Trade(
                    sell.orderId,
                    bestBid.orderId,
                    bestBidPrice,
                    fillQty
            ));

            sell.quantity -= fillQty;
            bestBid.quantity -= fillQty;

            if (bestBid.quantity == 0) {
                book.popBestBid();
            }
        }

        if (sell.quantity > 0) {
            book.add(sell);
        }

        return trades;
    }
}
