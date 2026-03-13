package com.kestrel.reservation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SeatInventory {

    private final Map<String, MutableSeat> seatsById = new LinkedHashMap<>();

    public SeatInventory(List<SeatDefinition> seats) {
        for (SeatDefinition seat : seats) {
            seatsById.put(seat.seatId(), new MutableSeat(seat.seatId(), seat.priceCents()));
        }
    }

    public SeatInventoryEntry getSeat(String seatId) {
        MutableSeat seat = seatsById.get(seatId);
        return seat == null ? null : seat.snapshot();
    }

    public boolean containsSeat(String seatId) {
        return seatsById.containsKey(seatId);
    }

    public boolean isAvailable(String seatId) {
        MutableSeat seat = seatsById.get(seatId);
        return seat != null && seat.state == SeatInventoryState.AVAILABLE;
    }

    public SeatInventoryEntry reserve(String seatId, ReservationRequest request) {
        MutableSeat seat = seatsById.get(seatId);
        if (seat == null || seat.state == SeatInventoryState.SOLD) {
            return null;
        }

        seat.state = SeatInventoryState.SOLD;
        seat.reservedByUserId = request.userId();
        seat.winningRequestId = request.requestId();
        seat.winningSequence = request.sequence();
        return seat.snapshot();
    }

    public int soldCount() {
        int sold = 0;
        for (MutableSeat seat : seatsById.values()) {
            if (seat.state == SeatInventoryState.SOLD) {
                sold++;
            }
        }
        return sold;
    }

    public int availableCount() {
        return seatsById.size() - soldCount();
    }

    public int totalSeats() {
        return seatsById.size();
    }

    private static final class MutableSeat {
        private final String seatId;
        private final long priceCents;
        private SeatInventoryState state;
        private String reservedByUserId;
        private Long winningRequestId;
        private Long winningSequence;

        private MutableSeat(String seatId, long priceCents) {
            this.seatId = seatId;
            this.priceCents = priceCents;
            this.state = SeatInventoryState.AVAILABLE;
        }

        private SeatInventoryEntry snapshot() {
            return new SeatInventoryEntry(
                    seatId,
                    priceCents,
                    state,
                    reservedByUserId,
                    winningRequestId,
                    winningSequence
            );
        }
    }
}
