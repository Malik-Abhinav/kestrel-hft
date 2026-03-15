package com.kestrel.api;

import com.kestrel.reservation.ReservationRequest;
import com.kestrel.reservation.ReservationResult;
import com.kestrel.reservation.ReservationStatus;
import com.kestrel.reservation.SeatDefinition;
import com.kestrel.reservation.SeatDropSimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DropStateService {

    private final AtomicLong dropCounter = new AtomicLong();
    private final Consumer<ReservationResult> resultListener;
    private StartDropRequest lastRequest;
    private String lastCompletedDropId;
    private DropState currentState = idleState();

    public DropStateService() {
        this(result -> { });
    }

    public DropStateService(Consumer<ReservationResult> resultListener) {
        this.resultListener = resultListener;
    }

    public synchronized DropState startDrop(StartDropRequest request) {
        StartDropRequest normalized = normalize(request);
        lastRequest = copyOf(normalized);
        return executeDrop(normalized, null);
    }

    public synchronized DropState replayLastDrop() {
        if (lastRequest == null) {
            throw new IllegalStateException("No completed drop is available to replay");
        }
        return executeDrop(copyOf(lastRequest), lastCompletedDropId);
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
                null,
                DropStatus.IDLE,
                false,
                0,
                0,
                0,
                0,
                0,
                0.0,
                0.0,
                0.0,
                List.of(),
                List.of()
        );
    }

    private DropState executeDrop(StartDropRequest request, String replayOfDropId) {
        long startedAt = System.nanoTime();
        SeatDropSimulation simulation = new SeatDropSimulation(request.seats());
        List<ReservationResult> results = simulation.run(request.requests(), resultListener);
        double elapsedMs = nanosToMillis(System.nanoTime() - startedAt);

        int soldCount = (int) results.stream()
                .filter(result -> result.status() == ReservationStatus.SOLD)
                .count();

        String dropId = "drop-" + dropCounter.incrementAndGet();
        currentState = new DropState(
                dropId,
                replayOfDropId,
                DropStatus.COMPLETED,
                true,
                results.size(),
                soldCount,
                results.size() - soldCount,
                simulation.inventory().totalSeats(),
                simulation.inventory().availableCount(),
                elapsedMs,
                averageLatencyMs(results),
                p95LatencyMs(results),
                simulation.inventory().entries(),
                results
        );
        lastCompletedDropId = dropId;
        return currentState;
    }

    private StartDropRequest copyOf(StartDropRequest request) {
        List<SeatDefinition> seats = new ArrayList<>(request.seats().size());
        for (SeatDefinition seat : request.seats()) {
            seats.add(new SeatDefinition(seat.seatId(), seat.priceCents()));
        }

        List<ReservationRequest> requests = new ArrayList<>(request.requests().size());
        for (ReservationRequest reservationRequest : request.requests()) {
            requests.add(new ReservationRequest(
                    reservationRequest.requestId(),
                    reservationRequest.sequence(),
                    reservationRequest.userId(),
                    reservationRequest.seatId()
            ));
        }
        return new StartDropRequest(List.copyOf(seats), List.copyOf(requests));
    }

    private double averageLatencyMs(List<ReservationResult> results) {
        if (results.isEmpty()) {
            return 0.0;
        }
        long totalLatencyMicros = 0;
        for (ReservationResult result : results) {
            totalLatencyMicros += result.latencyMicros();
        }
        return microsToMillis(totalLatencyMicros / (double) results.size());
    }

    private double p95LatencyMs(List<ReservationResult> results) {
        if (results.isEmpty()) {
            return 0.0;
        }
        List<Long> latencies = results.stream()
                .map(ReservationResult::latencyMicros)
                .sorted(Comparator.naturalOrder())
                .toList();
        int index = Math.max(0, (int) Math.ceil(latencies.size() * 0.95) - 1);
        return microsToMillis(latencies.get(index));
    }

    private double microsToMillis(double micros) {
        return micros / 1_000.0;
    }

    private double nanosToMillis(long nanos) {
        return nanos / 1_000_000.0;
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
