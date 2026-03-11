package com.kestrel;

import com.kestrel.reservation.ReservationRequest;
import com.kestrel.reservation.ReservationResult;
import com.kestrel.reservation.ReservationStatus;
import com.kestrel.reservation.SeatDefinition;
import com.kestrel.reservation.SeatDropSimulation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeatDropSimulationTest {

    @Test
    public void firstRequestWinsSeatAndDuplicateIsRejected() {
        SeatDropSimulation simulation = new SeatDropSimulation(List.of(
                new SeatDefinition("A1", 15_000)
        ));

        List<ReservationResult> results = simulation.run(List.of(
                new ReservationRequest(1, 1, "User_1", "A1"),
                new ReservationRequest(2, 2, "User_2", "A1")
        ));

        assertEquals(ReservationStatus.SOLD, results.get(0).status());
        assertEquals(ReservationStatus.REJECTED, results.get(1).status());
        assertEquals("A1", results.get(0).seatId());
        assertEquals("A1", results.get(1).seatId());
    }

    @Test
    public void unknownSeatIsRejectedWithoutAffectingSequence() {
        SeatDropSimulation simulation = new SeatDropSimulation(List.of(
                new SeatDefinition("A1", 15_000),
                new SeatDefinition("A2", 15_000)
        ));

        List<ReservationResult> results = simulation.run(List.of(
                new ReservationRequest(10, 10, "User_10", "A1"),
                new ReservationRequest(11, 11, "User_11", "Z9"),
                new ReservationRequest(12, 12, "User_12", "A2")
        ));

        assertEquals(ReservationStatus.SOLD, results.get(0).status());
        assertEquals(ReservationStatus.REJECTED, results.get(1).status());
        assertEquals(ReservationStatus.SOLD, results.get(2).status());
        assertEquals(10, results.get(0).sequence());
        assertEquals(11, results.get(1).sequence());
        assertEquals(12, results.get(2).sequence());
    }
}
