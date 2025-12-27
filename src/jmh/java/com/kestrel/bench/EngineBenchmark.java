package com.kestrel.bench;

import com.kestrel.core.Event;
import com.kestrel.core.EventPool;
import com.kestrel.engine.MatchingEngine;
import com.kestrel.orderbook.OrderBook;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class EngineBenchmark {

    @State(Scope.Thread)
    public static class CrossingState {
        private final EventPool pool = new EventPool(1024);
        private MatchingEngine engine;
        private OrderBook book;
        private Event incoming;

        @Setup(Level.Invocation)
        public void setup() {
            book = new OrderBook();
            engine = new MatchingEngine(book);

            Event ask = pool.get();
            ask.set(200, 100, 10, (byte) 0, (byte) 1);
            book.add(ask);

            incoming = pool.get();
            incoming.set(100, 100, 10, (byte) 1, (byte) 1);
        }
    }

    @State(Scope.Thread)
    public static class NonCrossingState {
        private final EventPool pool = new EventPool(1024);
        private MatchingEngine engine;
        private OrderBook book;
        private Event incoming;

        @Setup(Level.Invocation)
        public void setup() {
            book = new OrderBook();
            engine = new MatchingEngine(book);

            incoming = pool.get();
            incoming.set(100, 99, 10, (byte) 1, (byte) 1);
        }
    }

    @Benchmark
    public void matchCrossing(CrossingState state, Blackhole bh) {
        bh.consume(state.engine.onAddOrder(state.incoming));
    }

    @Benchmark
    public void addNonCrossing(NonCrossingState state, Blackhole bh) {
        bh.consume(state.engine.onAddOrder(state.incoming));
    }
}
