package com.kestrel.api;

import com.kestrel.reservation.ReservationRequest;
import com.kestrel.reservation.ReservationResult;
import com.kestrel.reservation.ReservationStatus;
import com.kestrel.reservation.SeatDefinition;
import com.kestrel.reservation.SeatDropSimulation;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DropStateService {

    private final AtomicLong dropCounter = new AtomicLong();
    private DropState currentState = idleState();

    public synchronized DropState startDrop(StartDropRequest request) {
        StartDropRequest normalized = normalize(request);
        SeatDropSimulation simulation = new SeatDropSimulation(normalized.seats());
        List<ReservationResult> results = simulation.run(normalized.requests());

        int soldCount = (int) results.stream()
                .filter(result -> result.status() == ReservationStatus.SOLD)
                .count();

        currentState = new DropState(
                "drop-" + dropCounter.incrementAndGet(),
                DropStatus.COMPLETED,
                results.size(),
                soldCount,
                results.size() - soldCount,
                simulation.inventory().totalSeats(),
                simulation.inventory().availableCount(),
                simulation.inventory().entries(),
                results
        );
        return currentState;
    }

    public synchronized DropState currentState() {
        return currentState;
    }

    private StartDropRequest normalize(StartDropRequest request) {
        if (request == null) {
            return defaultScenario();
        }

        List<SeatDefinition> seats = request.seats();
        List<ReservationRequest> requests = request.requests();
        if (seats == null || seats.isEmpty() || requests == null || requests.isEmpty()) {
            return defaultScenario();
        }
        return request;
    }

    private DropState idleState() {
        return new DropState(
                "drop-0",
                DropStatus.IDLE,
                0,
                0,
                0,
                0,
                0,
                List.of(),
                List.of()
        );
    }

    private StartDropRequest defaultScenario() {
        return new StartDropRequest(
                List.of(
                        new SeatDefinition("A1", 15_000),
                        new SeatDefinition("A2", 15_000),
                        new SeatDefinition("A3", 15_000),
                        new SeatDefinition("B1", 12_500)
                ),
                List.of(
                        new ReservationRequest(1, 1, "User_17", "A1"),
                        new ReservationRequest(2, 2, "User_22", "A1"),
                        new ReservationRequest(3, 3, "User_08", "A2"),
                        new ReservationRequest(4, 4, "User_55", "C9")
                )
        );
    }
}
