package com.kestrel.api;

import com.kestrel.reservation.ReservationResult;

public record LiveUpdateEvent(
        String seatId,
        String status,
        String userId,
        double latencyMs,
        long sequence
) {
    public static LiveUpdateEvent from(ReservationResult result) {
        return new LiveUpdateEvent(
                result.seatId(),
                result.status().name(),
                result.userId(),
                result.latencyMicros() / 1_000.0,
                result.sequence()
        );
    }
}
