package com.kestrel.api;

import com.kestrel.reservation.ReservationResult;
import com.kestrel.reservation.SeatInventoryEntry;

import java.util.List;

public record DropState(
        String dropId,
        String replayOfDropId,
        DropStatus status,
        boolean replayAvailable,
        int processedCount,
        int soldCount,
        int rejectedCount,
        int totalSeats,
        int availableSeats,
        double elapsedMs,
        double averageLatencyMs,
        double p95LatencyMs,
        List<SeatInventoryEntry> seats,
        List<ReservationResult> results
) {
}
