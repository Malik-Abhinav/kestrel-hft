# Kestrel Seat Drop Demo

Kestrel is a deterministic concert seat allocation demo built to show fair queue-order processing, low-latency event handling, and live observability in a compact Java system. The project now uses an explicit seat inventory and reservation processor as its primary backend path, while older prototype components remain in the repo during the migration.

## Problem

High-demand concert sales fail in predictable ways:

- many users try to claim the same small set of premium seats
- fairness claims are hard to explain without a clear processing model
- live systems are difficult to observe while the drop is happening
- once the rush ends, it is hard to replay or reason about what happened

Kestrel focuses on those problems with a narrow, credible story: a front-row concert seat drop where requests enter a waiting-room flow, are processed in deterministic order, and emit live status updates for observers.

## Solution

Kestrel models the seat-drop flow as a processor-centered system:

- users submit reservation attempts for specific seats
- requests are admitted and processed in deterministic queue order
- the processor is the source of truth for seat outcomes
- successful and rejected outcomes are published for live observation
- the architecture remains small enough to explain clearly in a short demo

The repository still contains older prototype internals that will be migrated over the next milestones. The primary seat-drop path is now expressed directly in reservation terms so future work can extend the product story without leaning on trading semantics.

## Product Definition

The demo product is a high-demand concert seat allocation system for premium seats.

Core scenario:

- a drop opens for front-row seats
- users arrive through a waiting-room style flow
- each user attempts to reserve a specific seat
- the backend processes requests in deterministic order after admission
- winners and rejections are visible through a live observation layer

Behavioral claims this repo is moving toward:

- fairness means deterministic queue-order processing after admission
- the processor decides winners; the UI does not
- observability is separate from the source of truth
- replay should preserve outcomes and processing order

## Terminology Plan

User-facing language from this point onward:

- `seat drop` instead of `market session`
- `reservation request` instead of `order`
- `reservation result` instead of `trade`
- `seat inventory` instead of `order book`
- `live updates` instead of `trade stream`
- `processor` instead of `matching engine`, where public-facing wording is involved

Some legacy package names still reflect the earlier prototype. Those changes are intentionally deferred so the repo remains runnable while the remaining web and live-observation layers are added in small steps.

## Current Technical Shape

Today the implementation still consists of:

- an explicit seat inventory model with per-seat sold/available state
- a reservation processor with first-request-wins allocation behavior
- a sample seat-drop simulation runner for local verification
- legacy transport and prototype engine packages that remain available during the migration
- Dockerized local setup and Gradle-based build/test tasks

This is acceptable for the current milestone because the source of truth now behaves like a reservation processor even though the HTTP and live observation layers are still pending.

## Project Layout

- `src/main/java/com/kestrel/reservation` seat inventory, reservation processor, and simulation runner
- `src/main/java/com/kestrel/engine` core runtime and processing entry points
- `src/main/java/com/kestrel/orderbook` legacy prototype state structures retained during migration
- `src/main/java/com/kestrel/buffer` ring buffer transport
- `src/main/java/com/kestrel/parser` ingest/parsing utilities from the prototype phase
- `src/jmh/java/com/kestrel/bench` JMH benchmarks
- `src/test/java/com/kestrel` unit tests

## Architecture Direction

Near-term architecture story:

1. requests enter an admission/waiting-room path
2. the reservation processor applies deterministic first-request-wins rules against seat inventory
3. inventory becomes the source of truth for sold and available seats
4. HTTP and live observation layers will expose those outcomes in later milestones

The current implementation already follows that source-of-truth model locally and will be exposed incrementally through APIs and live updates.

## Build and Run

### Local

```bash
./gradlew test
./gradlew run
```

`./gradlew run` now executes the sample seat-drop simulation path and prints reservation outcomes in sequence.

### Docker

```bash
docker compose up --build
```

## Observation Layer Status

The live observation layer is not wired into the reservation processor yet. That work is scheduled for the upcoming API and event-stream milestones, where reservation outcomes will be exposed over HTTP and then broadcast to connected clients.

## Benchmarks

Run the JMH suite:

```bash
./gradlew jmh
```

The existing benchmarks validate the performance characteristics of the underlying transport and processing primitives that will support the seat-drop story.

## Why This Project Exists

Kestrel is intended to demonstrate:

- deterministic event processing under bursty demand
- fair outcome reasoning through queue-order semantics
- separation of source-of-truth processing from observation channels
- pragmatic distributed systems design in a small, local-first project
