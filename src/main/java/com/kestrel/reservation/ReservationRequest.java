package com.kestrel.reservation;

public record ReservationRequest(
        long requestId,
        long sequence,
        String userId,
        String seatId
) {
}
