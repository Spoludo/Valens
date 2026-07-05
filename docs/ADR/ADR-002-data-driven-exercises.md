# ADR-002 – Data-driven Exercises

**Status:** Accepted  
**Date:** 2026-07-05

## Context

Valens should support future exercise packs without changing planner code.

## Decision

Exercises are defined as JSON/YAML data, not hardcoded classes.

## Consequences

Positive:

- extensibility
- community packs
- easier localization
- planner can reason from metadata

Negative:

- schema validation required
- more upfront design

The planner must never special-case exercise names.
