package com.kestrel.reservation;

import com.kestrel.core.Event;
import com.kestrel.engine.MatchingEngine;
import com.kestrel.engine.RedisTradePublisher;
import com.kestrel.engine.Trade;
import com.kestrel.orderbook.OrderBook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatReservationProcessor {

    private final SeatReservationMapper mapper = new SeatReservationMapper();
    private final Map<String, SeatLane> seatLanes = new HashMap<>();

    public SeatReservationProcessor(List<SeatDefinition> seats) {
        for (SeatDefinition seat : seats) {
            seatLanes.put(seat.seatId(), new SeatLane(seat, mapper));
        }
    }

    public ReservationResult process(ReservationRequest request) {
        SeatLane lane = seatLanes.get(request.seatId());
        long startedAt = System.nanoTime();
        if (lane == null) {
            return mapper.rejected(
                    request,
                    latencyMicros(startedAt),
                    "Seat does not exist in this drop"
            );
        }
        return lane.process(request, startedAt);
    }

    private long latencyMicros(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000;
    }

    private static final class SeatLane {
        private final SeatDefinition seat;
        private final SeatReservationMapper mapper;
        private final OrderBook book;
        private final MatchingEngine engine;

        private SeatLane(SeatDefinition seat, SeatReservationMapper mapper) {
            this.seat = seat;
            this.mapper = mapper;
            this.book = new OrderBook(8);
            this.engine = new MatchingEngine(book, RedisTradePublisher.fromEnv());
            this.book.add(mapper.toInventoryEvent(seat));
        }

        private ReservationResult process(ReservationRequest request, long startedAt) {
            Event attempt = mapper.toReservationAttempt(request, seat);
            List<Trade> trades = engine.onAddOrder(attempt);
            long latencyMicros = (System.nanoTime() - startedAt) / 1_000;

            if (!trades.isEmpty()) {
                return mapper.sold(request, trades.get(0), latencyMicros);
            }

            discardRejectedAttempt(attempt);
            return mapper.rejected(request, latencyMicros, "Seat is no longer available");
        }

        private void discardRejectedAttempt(Event attempt) {
            Event bestBid = book.bestBid();
            if (bestBid == attempt) {
                book.popBestBid();
            }
        }
    }
}
