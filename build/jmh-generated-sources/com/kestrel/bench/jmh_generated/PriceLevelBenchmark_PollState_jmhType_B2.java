package com.kestrel.bench.jmh_generated;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
public class PriceLevelBenchmark_PollState_jmhType_B2 extends PriceLevelBenchmark_PollState_jmhType_B1 {
    public volatile int setupTrialMutex;
    public volatile int tearTrialMutex;
    public final static AtomicIntegerFieldUpdater<PriceLevelBenchmark_PollState_jmhType_B2> setupTrialMutexUpdater = AtomicIntegerFieldUpdater.newUpdater(PriceLevelBenchmark_PollState_jmhType_B2.class, "setupTrialMutex");
    public final static AtomicIntegerFieldUpdater<PriceLevelBenchmark_PollState_jmhType_B2> tearTrialMutexUpdater = AtomicIntegerFieldUpdater.newUpdater(PriceLevelBenchmark_PollState_jmhType_B2.class, "tearTrialMutex");

    public volatile int setupIterationMutex;
    public volatile int tearIterationMutex;
    public final static AtomicIntegerFieldUpdater<PriceLevelBenchmark_PollState_jmhType_B2> setupIterationMutexUpdater = AtomicIntegerFieldUpdater.newUpdater(PriceLevelBenchmark_PollState_jmhType_B2.class, "setupIterationMutex");
    public final static AtomicIntegerFieldUpdater<PriceLevelBenchmark_PollState_jmhType_B2> tearIterationMutexUpdater = AtomicIntegerFieldUpdater.newUpdater(PriceLevelBenchmark_PollState_jmhType_B2.class, "tearIterationMutex");

    public volatile int setupInvocationMutex;
    public volatile int tearInvocationMutex;
    public final static AtomicIntegerFieldUpdater<PriceLevelBenchmark_PollState_jmhType_B2> setupInvocationMutexUpdater = AtomicIntegerFieldUpdater.newUpdater(PriceLevelBenchmark_PollState_jmhType_B2.class, "setupInvocationMutex");
    public final static AtomicIntegerFieldUpdater<PriceLevelBenchmark_PollState_jmhType_B2> tearInvocationMutexUpdater = AtomicIntegerFieldUpdater.newUpdater(PriceLevelBenchmark_PollState_jmhType_B2.class, "tearInvocationMutex");

}
