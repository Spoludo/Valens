# 17 – Assets

**Project:** Valens  
**Version:** 0.2  
**Status:** Phase 1 specification  
**Last updated:** 2026-07-05

---

## 1. Purpose

Assets include illustrations, body maps, audio and icons.

They should be replaceable and pack-friendly.

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

- body maps
- muscle overlays
- exercise diagrams
- icons

SVG scales well on phones and tablets.

---

## 4. Muscle maps

Use front and back body maps.

Muscle IDs must match exercise metadata.

Example:

```text
quadriceps
glutes
hamstrings
calves
core
deltoids
pectorals
latissimus
```

---

## 5. Joint maps

Joints should be visually markable for pain and load.

Examples:

```text
left_knee
right_knee
left_shoulder
right_shoulder
lumbar_spine
```

---

## 6. Exercise illustrations

Each exercise may include:

- setup
- active position
- common mistake
- easier variation
- harder variation

MVP may include placeholders.

---

## 7. Audio assets

MVP can use TTS and generated ticks.

Bundled audio later.

---

## 8. Licensing

All assets must have clear open-source-compatible licenses.

Do not include copyrighted images from videos.

---

## 9. Acceptance criteria

MVP assets:

- placeholder exercise illustrations
- basic body map
- basic icons
- simple audio ticks/beeps

---

## 10. Summary

Assets should educate and guide, but the core app must remain usable without perfect graphics.
