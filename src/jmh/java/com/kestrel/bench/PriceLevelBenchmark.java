package com.kestrel.bench;

import com.kestrel.core.Event;
import com.kestrel.core.EventPool;
import com.kestrel.orderbook.PriceLevel;
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

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class PriceLevelBenchmark {

    @State(Scope.Thread)
    public static class AddState {
        private final EventPool pool = new EventPool(1024);
        private PriceLevel level;
        private Event order;

        @Setup(Level.Invocation)
        public void setup() {
            level = new PriceLevel(1024);
            order = pool.get();
            order.set(1, 100, 10, (byte) 1, (byte) 1);
        }
    }

    @State(Scope.Thread)
    public static class PollState {
        private final EventPool pool = new EventPool(1024);
        private PriceLevel level;

        @Setup(Level.Invocation)
        public void setup() {
            level = new PriceLevel(1024);
            Event order = pool.get();
            order.set(1, 100, 10, (byte) 1, (byte) 1);
            level.add(order);
        }
    }

    @Benchmark
    public void addOrder(AddState state) {
        state.level.add(state.order);
    }

    @Benchmark
    public Event pollOrder(PollState state) {
        return state.level.poll();
    }
}
