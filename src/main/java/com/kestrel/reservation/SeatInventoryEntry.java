package com.kestrel.reservation;

public record SeatInventoryEntry(
        String seatId,
        long priceCents,
        SeatInventoryState state,
        String reservedByUserId,
        Long winningRequestId,
        Long winningSequence
) {
}
