package com.kestrel.reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SeatDropSimulation {

    private final SeatReservationProcessor processor;

    public SeatDropSimulation(List<SeatDefinition> seats) {
        this.processor = new SeatReservationProcessor(seats);
    }

    public List<ReservationResult> run(List<ReservationRequest> requests) {
        return run(requests, result -> { });
    }

    public List<ReservationResult> run(List<ReservationRequest> requests, Consumer<ReservationResult> resultListener) {
        List<ReservationResult> results = new ArrayList<>(requests.size());
        for (ReservationRequest request : requests) {
            ReservationResult result = processor.process(request);
            results.add(result);
            resultListener.accept(result);
        }
        return results;
    }

    public SeatInventory inventory() {
        return processor.inventory();
    }
}
