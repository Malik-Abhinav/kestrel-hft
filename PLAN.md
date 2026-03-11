# Kestrel Pivot Plan

This file tracks the active migration plan inside the repository so each new work session starts from the current milestone instead of repeating completed work.

## Status

- Day 1 completed on March 11, 2026
- Day 2 completed on March 11, 2026
- Current next milestone: Day 3 - Processor and Inventory Behavior

## Working Rules

- do not use day labels like `Day 1`, `Day 2`, or similar wording in commit messages
- prefer concise commit messages that describe the actual change

## Completed Work

### Day 1: Narrative Rebrand

Completed deliverables:

- README rewritten around concert seats, waiting room, fairness, and live observability
- `Problem` and `Solution` sections added
- clear product definition documented around front-row concert seats
- user-facing terminology plan documented in the README
- no major code changes introduced beyond documentation cleanup

Completed outcome:

- the repo now reads as a seat-allocation system directionally, even though the runtime migration is still in progress

### Day 2: Domain Bridge

Completed deliverables:

- reservation-domain types added for requests, results, status, and seat definitions
- seat/user-oriented result payload shape introduced in backend terms
- seat-drop simulation flow added through a reservation processor and simulation runner
- internal mapping added from seat reservations to the current matching-engine behavior
- `./gradlew run` now executes a basic seat-drop simulation path

Completed outcome:

- backend can now describe and process a drop in seat-allocation terms, even though the full inventory model and API layer are still pending

## Remaining Schedule

### Day 3: Processor and Inventory Behavior

Deliverables:

- explicit seat inventory model
- first-request-wins behavior for a seat
- rejection behavior for duplicate seat attempts
- cleanup of core logs and exposed names so HFT leftovers stop appearing
- tests for seat allocation and rejection rules

Outcome:

- the source of truth now behaves like a reservation processor, not a trading prototype

### Day 4: Web Server and API

Deliverables:

- add Javalin
- implement `POST /api/drop/start`
- implement `GET /api/drop/state`
- choose and lock request/response shapes
- backend remains runnable without frontend integration

Outcome:

- you can start a drop and inspect state through HTTP, which is a believable standalone checkpoint

### Day 5: Live Event Stream

Deliverables:

- Redis-backed event publication for reservation results
- WebSocket endpoint at `/ws/live-updates`
- rebroadcast flow from Redis to browser clients
- payload shape locked for live seat-sale events
- tests for publish/broadcast behavior where practical

Outcome:

- the system now has a live observation layer and a complete backend story

### Day 6: Latency Metrics and Replay

Deliverables:

- enqueue-to-process latency instrumentation
- summary stats: total processed, sold, rejected, elapsed time, average latency, p95 latency
- deterministic request-sequence capture
- replay endpoint or replay trigger for rerunning the last drop
- tests proving replay preserves winners and processing order

Outcome:

- the project becomes a reproducible fairness demo rather than just a live animation source

### Day 7: Frontend Dashboard

Deliverables:

- React + Tailwind frontend
- seat/stadium grid with 100-200 seats
- `Start Drop` button
- live feed showing sold seat and latency
- WebSocket client integration
- client-side buffered rendering so updates remain readable

Outcome:

- the demo becomes visually legible in under 30 seconds

### Day 8: Replay UX, Docker, and Polish

Deliverables:

- `Replay` button in UI
- summary modal with processed count, sold count, total time, and latency stats
- frontend added to `docker-compose.yml`
- final terminology scrub for README, UI, logs, and API text
- architecture diagram and GIF placeholder workflow

Outcome:

- one-command local demo, coherent story, and polished presentation

## Flex Days

### Day 9: Spillover / Hardening

Possible uses:

- fix bugs from WebSocket/frontend integration
- tighten replay correctness
- improve metrics formatting
- improve seat-grid pacing and UI stability
- add small missing tests

### Day 10: Presentation Polish

Possible uses:

- record the README GIF
- refine the summary modal copy
- improve README architecture diagram
- add interview notes and analogies
- clean commit history or split work into cleaner commits if needed

## Interfaces and Behavior To Lock Early

These should be stable by Day 4 or Day 5:

- `POST /api/drop/start`
- `GET /api/drop/state`
- `WS /ws/live-updates`
- optional `POST /api/drop/replay` by Day 6

Target live event payload:

```json
{
  "seatId": "Seat-42",
  "status": "SOLD",
  "userId": "User_99",
  "latencyMs": 0.45,
  "sequence": 123
}
```

Behavioral claims:

- fairness means deterministic queue-order processing after admission
- the processor is the source of truth
- Redis/WebSocket is the observation layer
- replay must preserve allocation outcomes and processing order

## Test Plan By Milestone

Day 2-3:

- available seat can be allocated
- duplicate attempt for same seat is rejected
- no trading vocabulary leaks through public-facing outputs

Day 4-5:

- start endpoint triggers a drop
- state endpoint returns useful metrics/state
- WebSocket clients receive reservation events
- Redis publication matches expected payload shape

Day 6:

- latency is captured from enqueue to processing
- replay reproduces winners and ordering
- summary metrics match processed events

Day 7-8:

- frontend grid reflects sold seats correctly
- buffered rendering stays readable under burst load
- replay reproduces the same visible sold-seat pattern
- summary modal shows correct totals and latency stats

## Assumptions and Defaults

- demo product is concert seats
- delivery target is 8 working days with 2 optional buffer days
- frontend stack is React + Tailwind
- backend web layer uses Javalin
- Redis remains in the architecture as the observation layer
- replay scope is deterministic rerun of the same request sequence, not full event persistence
- do not claim zero data loss or distributed fairness unless specifically implemented and measured
- each day should end with a believable, self-contained checkpoint that could reasonably exist as a separate commit or short commit series
