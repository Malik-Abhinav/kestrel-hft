package com.kestrel.reservation;

public record ReservationResult(
        long requestId,
        long sequence,
        String userId,
        String seatId,
        ReservationStatus status,
        long latencyMicros,
        String detail
) {
}
