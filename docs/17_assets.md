# 17 – Assets

**Project:** Valens
**Version:** 0.3
**Status:** Phase 1 specification
**Last updated:** 2026-07-06

---

## 1. Purpose

Assets include illustrations, body maps, joint maps, muscle overlays, audio and icons.

They should be replaceable, pack-friendly and optional.

The core app must remain usable even if some assets are missing.

---

## 2. Asset categories

```text
exercise_illustrations
muscle_maps
joint_maps
icons
audio
translations
```

---

## 3. SVG preference

Prefer SVG for:

* body maps
* muscle overlays
* joint overlays
* exercise diagrams
* icons

SVG scales well on phones and tablets.

Raster images may be used for temporary placeholders, but structured/vector assets are preferred for long-term maintainability.

---

## 4. Muscle maps

Use front and back body maps.

Muscle IDs must match exercise metadata.

Example muscle ids:

```text
quadriceps
glutes
hamstrings
calves
core
deltoids
pectorals
latissimus_dorsi
```

Muscle maps should support:

* highlighted primary muscles
* highlighted secondary muscles
* highlighted stabilizers
* different intensity levels
* front/back display
* future left/right distinction if needed

Exercise definitions may reference:

```text
assets/muscles/<exercise_id>_front.svg
assets/muscles/<exercise_id>_back.svg
```

---

## 5. Joint maps

Joints should be visually markable for pain, load and sensitivity.

Joint map identifiers should follow the side-aware joint model.

Exercise metadata uses anatomical joint ids plus side roles.

Pain reports use anatomical joint ids plus absolute side.

Example anatomical joint ids:

```text
knee
hip
shoulder
wrist
ankle
elbow
neck
lumbar_spine
```

Example absolute pain locations:

```text
jointId = knee
side = left

jointId = shoulder
side = right

jointId = lumbar_spine
side = midline
```

Example exercise-side roles:

```text
knee.bilateral
hip.workingSide
ankle.supportSide
shoulder.bilateral
wrist.bilateral
lumbar_spine.midline
```

Exercise definitions may reference:

```text
assets/joints/<exercise_id>_front.svg
assets/joints/<exercise_id>_back.svg
```

The rendering layer can decide how to display side roles:

```text
bilateral      -> left + right
midline        -> center structure
workingSide    -> scheduled working side
supportSide    -> scheduled support side
oppositeSide   -> opposite of scheduled working side
```

---

## 6. Exercise illustrations

Each exercise may include:

* setup
* active position
* common mistake
* easier variation
* harder variation

MVP may include placeholders.

Exercise definitions may reference:

```text
assets/illustrations/<exercise_id>.svg
assets/illustrations/<exercise_id>_start.svg
assets/illustrations/<exercise_id>_hold.svg
assets/illustrations/<exercise_id>_mistake.svg
```

Illustrations should prioritize clarity over anatomical perfection.

---

## 7. Audio assets

MVP can use:

* Text-to-speech
* generated ticks
* generated beeps

Bundled audio may be added later.

Possible future audio categories:

```text
countdown
phase_start
halfway
final_seconds
rest_start
exercise_complete
workout_complete
safety_cue
```

The audio engine should consume semantic events, not exercise-specific hardcoded strings.

---

## 8. Translations

Exercise packs may include translation files.

Example:

```text
exercise-packs/bundled/isometric-foundations/translations/en.json
```

Exercise definitions should reference localization keys rather than hardcoded display strings.

Example:

```json
{
  "nameKey": "exercise.wall_sit.name",
  "descriptionKey": "exercise.wall_sit.description"
}
```

---

## 9. Licensing

All assets must have clear open-source-compatible licenses.

Do not include:

* copyrighted images from videos
* copyrighted anatomy diagrams
* copied app screenshots
* proprietary audio files
* unlicensed icon packs

Generated or adapted assets should include source and license notes where practical.

---

## 10. Missing assets

Missing non-critical assets must not crash the app.

The MVP may use:

* generic exercise placeholders
* generic muscle map placeholders
* generic joint map placeholders
* generated beeps instead of bundled audio

The Exercise Library Engine should expose asset paths, but the UI must handle missing files gracefully.

---

## 11. Acceptance criteria

MVP assets:

* placeholder exercise illustrations
* basic body map
* basic joint map
* basic icons
* simple audio ticks/beeps
* no copyrighted assets
* app remains usable with missing optional assets

---

## 12. Summary

Assets should educate and guide, but Valens must not depend on perfect graphics.

The asset system should support the same core architecture as the exercise model:

> Exercises are metadata-defined, and assets are replaceable representations of that metadata.
