# ADR-001 – Offline First

**Status:** Accepted  
**Date:** 2026-07-05

## Context

Valens handles personal health-adjacent training data. The primary use case does not require internet access.

## Decision

Valens must function fully offline. No account or cloud service is required.

## Consequences

Positive:

- privacy
- reliability
- simplicity
- lower operating cost

Negative:

- no automatic sync initially
- device loss may lose data unless export exists

Future cloud sync must be optional.
