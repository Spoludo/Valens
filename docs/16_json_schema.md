# 16 – JSON Schema

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

Valens exercise packs are data-driven.

This document defines the role of JSON Schema in Valens and points to the canonical schema files.

The canonical JSON Schema files live in:

```text
schemas/

---

## 2. Exercise pack structure

```text
exercise-pack.json
movement-patterns.json
exercises/
    wall_sit.json
    hollow_hold.json
assets/
    ...
translations/
    en.json
    fr.json
```

---

## 3. Pack manifest

```json
{
  "id": "isometric_foundations",
  "version": "0.1.0",
  "name": "Isometric Foundations",
  "author": "Valens",
  "schemaVersion": "0.1",
  "minAppVersion": "0.1.0"
}
```

---

## 4. Exercise definition skeleton

```json
{
  "id": "wall_sit",
  "type": "isometric",
  "movementPatternId": "squat",
  "exerciseFamilyId": "wall_sit",
  "difficulty": 2,
  "equipment": ["wall"],
  "defaultPrescription": {},
  "muscles": {},
  "jointStress": {},
  "progression": [],
  "regressions": [],
  "alternatives": [],
  "cues": {},
  "assets": {}
}
```

---

## 5. Validation

On startup:

- load packs
- validate schema version
- validate required fields
- validate referenced movement patterns
- validate assets exist
- disable invalid pack with clear error

Invalid exercise definitions must not crash the app.

---

## 6. Versioning

All packs require semantic versioning.

Breaking schema changes require migration or compatibility layer.

---

## 7. Localization

Display strings should be keys, not hardcoded text.

Example:

```json
"nameKey": "exercise.wall_sit.name"
```

---

## 8. Acceptance criteria

MVP must load bundled JSON exercises and validate required fields.

---

## 9. Summary

The JSON schema is the foundation for Valens as an open platform.
