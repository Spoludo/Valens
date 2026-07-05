# ADR-003 – Planner Independence

**Status:** Accepted  
**Date:** 2026-07-05

## Context

The planner is the core intelligence of Valens and must be testable.

## Decision

Planner logic must be pure Kotlin domain logic independent from Android UI.

## Consequences

Positive:

- unit testing
- reproducibility
- portability
- easier debugging

Negative:

- requires careful dependency boundaries
