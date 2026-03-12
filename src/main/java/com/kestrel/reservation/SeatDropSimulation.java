package com.kestrel.reservation;

import java.util.ArrayList;
import java.util.List;

public class SeatDropSimulation {

    private final SeatReservationProcessor processor;

    public SeatDropSimulation(List<SeatDefinition> seats) {
        this.processor = new SeatReservationProcessor(seats);
    }

    public List<ReservationResult> run(List<ReservationRequest> requests) {
        List<ReservationResult> results = new ArrayList<>(requests.size());
        for (ReservationRequest request : requests) {
            results.add(processor.process(request));
        }
        return results;
    }

    public SeatInventory inventory() {
        return processor.inventory();
    }
}
