package com.kestrel.reservation;

import java.util.List;

public class SeatReservationProcessor {

    private final SeatInventory inventory;

    public SeatReservationProcessor(List<SeatDefinition> seats) {
        this(new SeatInventory(seats));
    }

    public SeatReservationProcessor(SeatInventory inventory) {
        this.inventory = inventory;
    }

    public ReservationResult process(ReservationRequest request) {
        long startedAt = System.nanoTime();

        if (!inventory.containsSeat(request.seatId())) {
            return rejected(request, latencyMicros(startedAt), "Seat does not exist in this drop");
        }

        SeatInventoryEntry reservedSeat = inventory.reserve(request.seatId(), request);
        if (reservedSeat == null) {
            return rejected(request, latencyMicros(startedAt), "Seat is no longer available");
        }

        return sold(request, reservedSeat, latencyMicros(startedAt));
    }

    public SeatInventory inventory() {
        return inventory;
    }

    private ReservationResult sold(ReservationRequest request, SeatInventoryEntry seat, long latencyMicros) {
        return new ReservationResult(
                request.requestId(),
                request.sequence(),
                request.userId(),
                request.seatId(),
                ReservationStatus.SOLD,
                latencyMicros,
                "Seat reserved at " + seat.priceCents() + " cents"
        );
    }

    private ReservationResult rejected(ReservationRequest request, long latencyMicros, String detail) {
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

    private long latencyMicros(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000;
    }
}
