# Kestrel HFT Engine

A low-latency matching engine prototype with a lock-free ring buffer ingest path, an in-memory order book, and Redis trade publishing for downstream consumers. Built to demonstrate systems performance, concurrency, and distributed messaging in a compact Java project.

## Highlights

- Single-producer/single-consumer ring buffer (Agrona) for event transport
- Matching engine with price-time priority across bid/ask books
- ITCH-style parser for add-order messages
- JMH microbenchmarks for throughput validation
- Redis trade publisher for distributed trade reporting
- Dockerized build and compose stack

## Project Layout

- `src/main/java/com/kestrel/engine` Matching engine and runtime entry points
- `src/main/java/com/kestrel/orderbook` Order book and price levels
- `src/main/java/com/kestrel/buffer` Ring buffer transport
- `src/main/java/com/kestrel/parser` ITCH parser
- `src/jmh/java/com/kestrel/bench` JMH benchmarks
- `src/test/java/com/kestrel` Unit tests

## Architecture (High Level)

1) ITCH parser reads events
2) Producer publishes add orders into a ring buffer
3) Consumer thread feeds `MatchingEngine`
4) Trades are emitted to Redis on `kestrel:trades`

## Build and Run

### Local

```powershell
.\gradlew test
.\gradlew run
```

### Docker

```powershell
docker compose up --build
```

## Redis Trade Stream

Trades are published as JSON to `kestrel:trades` when matches occur.

Example payload:

```json
{"takerOrderId":10663,"makerOrderId":663,"price":103,"quantity":10}
```

Subscribe to the channel:

```powershell
docker exec -it kestrel-hft-redis-1 redis-cli SUBSCRIBE kestrel:trades
```

Environment variables:

- `REDIS_ENABLED` set to `true` to enable publishing
- `REDIS_HOST` default `localhost`
- `REDIS_PORT` default `6379`
- `REDIS_CHANNEL` default `kestrel:trades`

## Benchmarks

Run the JMH suite:

```powershell
.\gradlew jmh
```

Benchmarks include:

- ring buffer publish/consume throughput
- matching engine crossing vs non-crossing orders
- price level queue add/poll performance

## Resume-Ready Talking Points

- Lock-free SPSC ring buffer ingestion path using Agrona
- Matching engine with price-time priority and in-memory order book
- Trade reporting via Redis pub/sub (distributed systems integration)
- JMH microbenchmarks for latency/throughput characterization
- Containerized build and deployment workflow
