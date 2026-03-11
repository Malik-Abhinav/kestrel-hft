package com.kestrel.reservation;

import com.kestrel.core.Event;
import com.kestrel.engine.Trade;

final class SeatReservationMapper {

    private static final byte BUY_SIDE = 1;
    private static final byte SELL_SIDE = 0;
    private static final byte ADD_TYPE = 1;

    Event toInventoryEvent(SeatDefinition seat) {
        Event inventory = new Event();
        inventory.set(inventoryOrderId(seat.seatId()), seat.priceCents(), 1, SELL_SIDE, ADD_TYPE);
        return inventory;
    }

    Event toReservationAttempt(ReservationRequest request, SeatDefinition seat) {
        Event attempt = new Event();
        attempt.set(request.requestId(), seat.priceCents(), 1, BUY_SIDE, ADD_TYPE);
        return attempt;
    }

    ReservationResult sold(ReservationRequest request, Trade trade, long latencyMicros) {
        return new ReservationResult(
                request.requestId(),
                request.sequence(),
                request.userId(),
                request.seatId(),
                ReservationStatus.SOLD,
                latencyMicros,
                "Seat reserved at " + trade.price + " cents"
        );
    }

    ReservationResult rejected(ReservationRequest request, long latencyMicros, String detail) {
        return new ReservationResult(
                request.requestId(),
                request.sequence(),
                request.userId(),
                request.seatId(),
                ReservationStatus.REJECTED,
                latencyMicros,
                detail
        );
    }

    private long inventoryOrderId(String seatId) {
        return Integer.toUnsignedLong(seatId.hashCode());
    }
}
