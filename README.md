# Kestrel Seat Drop Demo

Kestrel is a deterministic concert seat allocation demo built to show fair queue-order processing, low-latency event handling, and live observability in a compact Java system. The project is being repositioned from its matching-engine roots into a focused demo for high-demand ticket drops.

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

The current codebase still contains matching-engine internals that will be migrated over the next milestones. This repository now documents the product and behavioral direction explicitly so future changes stay aligned with the seat-allocation narrative.

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

Internal class names and engine semantics still reflect the earlier prototype. Those changes are intentionally deferred to later milestones so the repo remains runnable while the domain migration happens in small steps.

## Current Technical Shape

Today the implementation still consists of:

- a lock-free single-producer/single-consumer ring buffer ingest path
- an in-memory matching-style core that will be mapped to seat allocation behavior
- Redis publication for downstream event observation
- Dockerized local setup and Gradle-based build/test tasks

This is acceptable for the current milestone because the work so far is a narrative and public-surface rebrand, not a full runtime conversion.

## Project Layout

- `src/main/java/com/kestrel/engine` core runtime and processing entry points
- `src/main/java/com/kestrel/orderbook` in-memory state structures slated for seat-inventory migration
- `src/main/java/com/kestrel/buffer` ring buffer transport
- `src/main/java/com/kestrel/parser` ingest/parsing utilities from the prototype phase
- `src/jmh/java/com/kestrel/bench` JMH benchmarks
- `src/test/java/com/kestrel` unit tests

## Architecture Direction

Near-term architecture story:

1. requests enter an admission/waiting-room path
2. accepted requests are published into the ring buffer
3. a single processor thread applies deterministic allocation rules
4. outcomes are emitted to Redis and later to live clients

The current implementation is partway to that target and will be bridged incrementally.

## Build and Run

### Local

```bash
./gradlew test
./gradlew run
```

### Docker

```bash
docker compose up --build
```

## Event Publication

The current runtime publishes JSON events to Redis. During the transition period, those events still come from matching-style internals, but this channel will become the live reservation update stream for the seat-drop demo.

Default Redis channel:

```text
kestrel:trades
```

Example current payload:

```json
{"takerOrderId":10663,"makerOrderId":663,"price":103,"quantity":10}
```

Subscribe locally:

```bash
docker exec -it kestrel-hft-redis-1 redis-cli SUBSCRIBE kestrel:trades
```

Environment variables:

- `REDIS_ENABLED` set to `true` to enable publishing
- `REDIS_HOST` default `localhost`
- `REDIS_PORT` default `6379`
- `REDIS_CHANNEL` default `kestrel:trades`

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
