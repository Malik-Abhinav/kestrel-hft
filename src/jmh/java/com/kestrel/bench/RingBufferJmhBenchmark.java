package com.kestrel.bench;

import com.kestrel.buffer.RingBufferBus;
import com.kestrel.buffer.RingBufferFactory;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class RingBufferJmhBenchmark {

    @State(Scope.Group)
    public static class RingState {
        private OneToOneRingBuffer ring;
        private RingBufferBus bus;
        private long seq;

        @Setup(Level.Iteration)
        public void setup() {
            ring = RingBufferFactory.create(1024 * 1024);
            bus = new RingBufferBus(ring);
            seq = 0;
        }
    }

    @Group("ring")
    @GroupThreads(1)
    @Benchmark
    public void publish(RingState state) {
        long id = state.seq++;
        while (!state.bus.publishAdd((byte) 1, id, 100L, 10)) {
            Thread.onSpinWait();
        }
    }

    @Group("ring")
    @GroupThreads(1)
    @Benchmark
    public void consume(RingState state, Blackhole bh) {
        state.bus.poll((msgType, side, orderId, price, qty) -> {
            bh.consume(orderId);
        }, 1024);
    }
}
