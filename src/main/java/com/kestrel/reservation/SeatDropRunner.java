package com.kestrel.reservation;

import java.util.List;

public class SeatDropRunner {

    public static void main(String[] args) {
        List<SeatDefinition> seats = List.of(
                new SeatDefinition("A1", 15_000),
                new SeatDefinition("A2", 15_000),
                new SeatDefinition("A3", 15_000),
                new SeatDefinition("B1", 12_500)
        );

        List<ReservationRequest> requests = List.of(
                new ReservationRequest(1, 1, "User_17", "A1"),
                new ReservationRequest(2, 2, "User_22", "A1"),
                new ReservationRequest(3, 3, "User_08", "A2"),
                new ReservationRequest(4, 4, "User_55", "C9")
        );

        SeatDropSimulation simulation = new SeatDropSimulation(seats);
        List<ReservationResult> results = simulation.run(requests);

        long soldCount = results.stream()
                .filter(result -> result.status() == ReservationStatus.SOLD)
                .count();

        for (ReservationResult result : results) {
            System.out.printf(
                    "sequence=%d user=%s seat=%s status=%s latencyMicros=%d detail=%s%n",
                    result.sequence(),
                    result.userId(),
                    result.seatId(),
                    result.status(),
                    result.latencyMicros(),
                    result.detail()
            );
        }

        System.out.printf("processed=%d sold=%d rejected=%d%n",
                results.size(),
                soldCount,
                results.size() - soldCount);
        System.out.printf("inventoryTotal=%d inventorySold=%d inventoryAvailable=%d%n",
                simulation.inventory().totalSeats(),
                simulation.inventory().soldCount(),
                simulation.inventory().availableCount());
    }
}
