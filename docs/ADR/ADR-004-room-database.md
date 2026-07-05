# ADR-004 – Room Database

**Status:** Proposed  
**Date:** 2026-07-05

## Context

Valens needs local persistence for structured historical data.

## Decision

Use Room as the default local database layer.

## Consequences

Positive:

- Android-native
- schema migrations
- query support
- Flow integration

Negative:

- Android-specific
- requires migration discipline
