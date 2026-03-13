package com.kestrel.api;

import com.kestrel.reservation.ReservationResult;
import com.kestrel.reservation.SeatInventoryEntry;

import java.util.List;

public record DropState(
        String dropId,
        DropStatus status,
        int processedCount,
        int soldCount,
        int rejectedCount,
        int totalSeats,
        int availableSeats,
        List<SeatInventoryEntry> seats,
        List<ReservationResult> results
) {
}
