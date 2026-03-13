package com.kestrel.api;

import com.kestrel.reservation.ReservationRequest;
import com.kestrel.reservation.SeatDefinition;

import java.util.List;

public record StartDropRequest(
        List<SeatDefinition> seats,
        List<ReservationRequest> requests
) {
}
