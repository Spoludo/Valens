# 20 – Technology Stack

**Project:** Valens  
**Version:** 0.1  
**Status:** Draft

## Purpose

This document defines the implementation technology stack for Valens.

Versions should be selected at implementation time using the latest stable versions compatible with the chosen Android Gradle Plugin and Kotlin version.

Avoid hardcoding exact versions in documentation unless required for compatibility.

## Android platform

- Minimum SDK: 28 unless implementation constraints require otherwise
- Target SDK: latest stable available at project creation
- Compile SDK: latest stable available at project creation

## Language and build

- Kotlin
- Gradle Kotlin DSL
- Android Gradle Plugin
- Version catalog: `gradle/libs.versions.toml`

## UI

- Jetpack Compose
- Material 3
- Navigation Compose
- AndroidX Lifecycle ViewModel Compose

## Architecture

- MVVM
- Repository pattern
- Use cases where helpful
- Coroutines
- Flow

## Persistence

- Room
- DataStore Preferences

Room stores structured user history.

DataStore stores lightweight user preferences.

## Serialization

- kotlinx.serialization

Used for exercise packs, movement patterns, planner snapshots and export/import.

## Dependency injection

Initial approach:

- Manual dependency injection

Possible future approach:

- Hilt

Do not introduce Hilt before the project has enough complexity to justify it.

## Image and SVG handling

Preferred:

- Android VectorDrawable for static vector assets
- SVG conversion pipeline where practical
- Coil only if runtime image loading becomes necessary

Do not add Coil by default unless the implementation actually needs async image loading.

## Audio

- Android TextToSpeech
- SoundPool or lightweight audio playback for ticks/beeps

Avoid complex audio libraries for MVP.

## Testing

- JUnit
- Kotlin test utilities
- Coroutines test
- Turbine for Flow tests
- Room testing
- Compose UI test

MockK may be used, but prefer fakes for domain tests.

## Static analysis and formatting

Use at least one formatting/static analysis tool:

- ktlint or Spotless with ktlint
- Detekt

Do not overconfigure initially.

## CI

Use GitHub Actions.

Required checks:

- Gradle build
- unit tests
- lint
- ktlint/format check
- detekt if configured

## Documentation

- Markdown
- Mermaid diagrams where useful
- KDoc for public domain types and non-trivial algorithms

## Dependency policy

Prefer official AndroidX and JetBrains libraries.

Avoid adding dependencies for small utilities.

Any third-party dependency must answer:

1. What problem does it solve?
2. Is it maintained?
3. Is it compatible with open-source distribution?
4. Can we remove it later?
5. Does it increase app size or complexity significantly?

## Initial Gradle dependency categories

The implementation should include dependencies for:

- Compose BOM
- Material 3
- Navigation Compose
- Lifecycle ViewModel Compose
- Room runtime/compiler/ktx
- DataStore Preferences
- kotlinx.serialization JSON
- Coroutines Android/test
- JUnit
- Compose UI testing

Exact coordinates and versions should be generated in `libs.versions.toml`.

## Summary

The stack should remain modern, boring and maintainable.

Do not add technology because it is fashionable. Add it because Valens needs it.
