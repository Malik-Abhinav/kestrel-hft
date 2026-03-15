package com.kestrel.bench;

import com.kestrel.reservation.ReservationRequest;
import com.kestrel.reservation.SeatDefinition;
import com.kestrel.reservation.SeatReservationProcessor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class SeatReservationBenchmark {

    @State(Scope.Thread)
    public static class AvailableSeatState {
        private SeatReservationProcessor processor;
        private ReservationRequest request;

        @Setup(Level.Invocation)
        public void setup() {
            processor = new SeatReservationProcessor(List.of(
                    new SeatDefinition("A1", 15_000)
            ));
            request = new ReservationRequest(1, 1, "User_1", "A1");
        }
    }

    @State(Scope.Thread)
    public static class DuplicateSeatState {
        private SeatReservationProcessor processor;
        private ReservationRequest duplicateRequest;

        @Setup(Level.Invocation)
        public void setup() {
            processor = new SeatReservationProcessor(List.of(
                    new SeatDefinition("A1", 15_000)
            ));
            processor.process(new ReservationRequest(1, 1, "User_1", "A1"));
            duplicateRequest = new ReservationRequest(2, 2, "User_2", "A1");
        }
    }

    @Benchmark
    public void reserveAvailableSeat(AvailableSeatState state, Blackhole bh) {
        bh.consume(state.processor.process(state.request));
    }

    @Benchmark
    public void rejectDuplicateSeatAttempt(DuplicateSeatState state, Blackhole bh) {
        bh.consume(state.processor.process(state.duplicateRequest));
    }
}
