package com.kestrel.reservation;

public record SeatDefinition(
        String seatId,
        long priceCents
) {
}
