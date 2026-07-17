# Skeleton/Armature Pose Model — Design

**Status:** Approved
**Scope:** Replace the workout screen's flat, hand-authored 2D point-map pose model (`BodyPoint`/`BodyPose`/`RoutineExercisePoses`, from `docs/superpowers/specs/2026-07-14-workout-guidance-design.md` §3–§8) with a small articulated skeleton/armature model: one set of joint angles per exercise, resolved via lightweight forward kinematics into 3D-ish joint positions, then projected into side/front/back 2D views. This supersedes Part A of the prior spec entirely. **Part B of the prior spec (spoken TextToSpeech cues, §9–§14) is unaffected and stays as documented there** — this spec does not touch `workout/audio/**`.

## 1. Why

The point-map model hand-placed ~20 raw coordinates per exercise, and getting `wall_push`'s second ("Front") view meant hand-authoring an entirely separate, geometrically-unrelated set of ~20 more coordinates. There was no real skeleton underneath — just point clouds that happened to look similar. On-device testing of the resting/back-ish views also showed the figure reads as less natural than it should, because nothing in the model kept a persistent shoulder line or pelvis line — those emerged by accident from wherever the shoulder/hip points happened to be placed, not from a real structure.

## 2. Direction: a lightweight pseudo-3D armature, not a point cloud

Modeled after a sculptor's wire armature (see reference: Andrew Joseph Keith, *How To Make Aluminum Wire Armatures*), used here **only** for its skeleton structure, joint layout, and cranial-unit proportions — not its physical wire-bending construction process, which is irrelevant to this app.

- One skeleton (bone lengths, fixed) + one set of joint angles per exercise (a `SkeletonPose`) — not one point-set per exercise per view.
- Forward kinematics (FK) resolves the skeleton + pose angles into 3D-ish joint positions, in **cranial units** (defined below), using an internal `Vec3` type.
- A projector drops one axis per requested view to get 2D screen-space points (`ProjectedPoint`): **side** view drops the sideways axis, **front** drops depth, **back** is front mirrored.
- Side view naturally collapses left/right into one silhouette (the dropped axis *is* left/right); front/back naturally show true bilateral symmetry. This is not simulated per-view — it falls straight out of the geometry, which is the entire point of moving off the old model.

**Explicitly not built:** a 3D graphics engine, rotation matrices/quaternions (plain `sin`/`cos` per joint is enough), inverse kinematics, physics, torso twist, sideways lean, full 3-axis rotation on every joint, or any external library (no camera, ML Kit, Rive, Lottie, Spine, PICO/XR — all already excluded, staying excluded).

**Keeping the skeleton core Compose-independent:** `Vec3` and `ProjectedPoint` are plain Kotlin data classes — no `androidx.compose.ui.geometry.Offset` anywhere in `workout/pose/skeleton`. The `androidx.compose.ui:ui-geometry` dependency was previously required just to unit-test the pose math (since `BodyPose` stored `Offset` directly); with `Vec3`/`ProjectedPoint`, the entire FK/projection/interpolation layer is pure Kotlin, testable with zero Compose on the classpath. Only the final rendering step (`ui/workout/BodyPoseIllustration.kt`) converts a `ProjectedPoint` to a Canvas-space `Offset`.

## 3. Proportions: cranial units, not head-units

**Correction from an earlier draft of this design:** proportions are **not** "head-units, ~7.5 heads tall" (a figure-*drawing* convention). They're **cranial units**, a sculptor's *armature* convention — same idea (use the head as the ruler), different, taller ratio: an armature figure is roughly **11.5–12 cranial units tall**, deliberately narrow through the shoulders and hips (clay is added later; the wire must not get in its own way). Values below, traced along the same path the reference armature's wire follows (crown → sternum → arm → sternum → spine → leg → spine → other leg → spine → other arm → crown):

| Segment | Length (cranial units) | Notes |
|---|---|---|
| Head | 1.0 (diameter) | radius 0.5; the unit itself is defined by head size |
| Neck (head-bottom → sternal notch) | 1.0 | crown-to-sternum is 2.0 total; head diameter (1.0) accounts for the rest |
| Shoulder half-width (sternum → shoulder joint, lateral) | 1.0 | |
| Upper arm (shoulder → elbow) | 2.0 | |
| Forearm (elbow → wrist) | 2.0 | matches the reference's "elbow to knuckles" |
| Hand (wrist → fingertip) | 0.5 | **not** in the reference (its wire loop *is* the hand, undifferentiated) — added so hand is an explicit segment per design goal 5; kept short and clearly a minor addition, not a sourced measurement |
| Spine (sternal notch → sacrum) | 3.5 | |
| Pelvis half-width (sacrum → hip joint, lateral) | 0.6 | reference armature: keep hips narrow — "a half unit or two-thirds of a unit," not the full 1 unit first suggested |
| Thigh (hip → knee) | 3.0 | |
| Shin (knee → ankle) | 3.0 | reference measures knee-to-*ground*; foot height is treated as negligible, so shin runs knee-to-ankle |
| Heel (ankle → heel tip, **backward**) | 0.33 | |
| Toe (ankle → toe tip, **forward**) | 1.0 | |

Total height (crown to sole): 1.0 + 1.0 + 3.5 + 3.0 + 3.0 = **11.5 cranial units** — matches the reference's stated 11–12 unit range.

**Why the foot is a fork, not a continuing bone (unlike the hand):** the reference gives *two* measurements from the ankle — heel (backward) and toe (forward) — because a standing foot genuinely extends both behind and in front of the ankle. A hand has no equivalent "behind the wrist" extent worth modeling. So arms render as a clean 3-segment polyline (shoulder→elbow→wrist→hand-tip); legs render as a 2-segment polyline to the ankle (hip→knee→ankle) **plus** a 2-segment fork at the ankle (ankle→heel-tip, ankle→toe-tip). This is a deliberate asymmetry between arms and legs, not an oversight — documenting it here so it doesn't read as inconsistent later.

These are proportional *references* for a readable, legible armature — not a claim of sculptural/anatomical precision. "Good enough to represent exercise positions clearly" is the bar, per the original ask.

## 4. Core types (`workout/pose/skeleton/`)

```kotlin
data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
}
```

Axis convention: `+x` = viewer's right (when facing the figure), `+y` = up, `+z` = toward the viewer (out of the front of the figure — e.g. the direction `wall_push`'s hands reach toward). All three axes are in cranial units.

```kotlin
data class JointRotation(
    val flexionDegrees: Float = 0f,   // bend in the sagittal (side-view) plane
    val abductionDegrees: Float = 0f, // spread in the frontal (front-view) plane — ignored for hinge joints
)
```

Every **limb** bone (upper arm, forearm, hand, thigh, shin) has a rest direction of straight down (`Vec3(0f, -1f, 0f)`) — a relaxed arm/leg hangs down. A `JointRotation` rotates that rest direction: flexion rotates it within the y-z plane (around the x-axis) — this is what makes an arm swing forward/back or a knee bend; abduction rotates the result within the x-y plane (around the z-axis) — this is what makes an arm spread sideways or a leg abduct. The rotated unit direction is scaled by the bone's length (§3) and added to the parent joint's resolved position. Elbow/knee only ever set `flexionDegrees` (their `abductionDegrees` stays 0 — enforced by giving those joints no abduction parameter at the `SkeletonPose` call-site level, not by a runtime check, so a misuse is a compile error, not a silent no-op).

The **spine** (and neck/head, which continue the same line) is the opposite case: its rest direction is straight *up* (`Vec3(0f, 1f, 0f)`) — an upright torso stands up from the pelvis. `rootLeanDegrees` rotates this rest direction the same way flexion does (y-z plane, around the x-axis), tilting the whole upper body forward or back. Heel/toe are not rotated at all — they're fixed offsets from the ankle along a constant backward/forward (z) direction (§3), since ankle has no `JointRotation` this round.

```kotlin
data class SkeletonPose(
    val rootLeanDegrees: Float = 0f,       // whole-body lean, sagittal plane only
    val leftShoulder: JointRotation = JointRotation(),
    val rightShoulder: JointRotation = JointRotation(),
    val leftElbowDegrees: Float = 0f,       // flexion-only joints: a bare Float, not a JointRotation
    val rightElbowDegrees: Float = 0f,
    val leftHip: JointRotation = JointRotation(),
    val rightHip: JointRotation = JointRotation(),
    val leftKneeDegrees: Float = 0f,
    val rightKneeDegrees: Float = 0f,
)
```

The **neutral pose is `SkeletonPose()`** — every angle defaulting to zero — replacing the old model's hand-authored 20-point `neutralStandingPose`. Wrist and ankle have no entry at all (per design goal: "wrist/ankle: simple for now" — hand/foot are fixed continuations of the forearm/shin direction, not independently posable this round).

**Root lean and the legs — a modeling detail future pose authors need to know:** `rootLeanDegrees` tilts the pelvis and everything built on top of it (spine, shoulders, arms, head). Legs are *also* attached at the pelvis, with their own hip `JointRotation` measured relative to the pelvis's (now-tilted) orientation. There's no automatic "keep feet on the ground" IK — if a pose leans the root forward (e.g. `wall_push`), the author must set hip flexion to compensate (roughly the negative of the root lean) so the legs still read as vertical/grounded. This is a per-pose authoring responsibility, not a framework guarantee, and is called out explicitly in §6 for the poses where it applies.

## 5. Forward kinematics & projection (`workout/pose/skeleton/`)

```kotlin
enum class SkeletonJoint {
    Head, Neck, SpineTop, Pelvis,
    LeftShoulder, RightShoulder, LeftElbow, RightElbow, LeftWrist, RightWrist, LeftHand, RightHand,
    LeftHip, RightHip, LeftKnee, RightKnee, LeftAnkle, RightAnkle,
    LeftHeel, RightHeel, LeftToe, RightToe,
}

fun resolve(pose: SkeletonPose): Map<SkeletonJoint, Vec3>
```

`resolve` walks the hierarchy exactly as described in §4: `Pelvis` is the root (origin); `SpineTop` is `Pelvis + rootLean-rotated spine direction` (spine's rest direction is up, §4); `LeftShoulder`/`RightShoulder` are lateral offsets from `SpineTop` (± shoulder half-width along the *root-lean-rotated* x-axis — the shoulder line itself doesn't independently rotate, it's carried by the spine/root lean); `Neck`/`Head` continue further up from `SpineTop`, same rotated-up direction; each arm/leg chain applies its own `JointRotation`s as described in §4; `LeftHip`/`RightHip` are lateral offsets from `Pelvis` the same way shoulders are; heel/toe are the two fixed forks off each ankle. 22 joints resolved per pose (`SkeletonJoint` has 22 entries — vs. the old model's 20 raw points; similar order of magnitude, now fully derived rather than hand-placed).

```kotlin
data class ProjectedPoint(val x: Float, val y: Float)

fun project(joints: Map<SkeletonJoint, Vec3>, angle: PoseViewAngle): Map<SkeletonJoint, ProjectedPoint>
```

- `SIDE`: `(z, -y)` per joint (drops x; y negated because cranial-unit "up" is `+y` but screen-space "up" is smaller values).
- `FRONT`: `(x, -y)` (drops z).
- `BACK`: `(-x, -y)` (drops z, mirrored — viewing from behind flips apparent left/right).

Output is still in cranial units, not yet fitted to a Canvas — see §7.

## 6. `PoseViewAngle`, `BodyPoseView`, and the pose catalog (`workout/pose/`)

```kotlin
enum class PoseViewAngle { SIDE, FRONT, BACK }
enum class PoseProp { NONE, WALL, FLOOR }

data class BodyPoseView(
    val angle: PoseViewAngle,
    val label: String,
    val pose: SkeletonPose,
    val prop: PoseProp = PoseProp.NONE,
)
```

**`prop` moves from the pose to the view** (it lived on `BodyPose` in the old model). This is a deliberate change: `wall_push`'s `SIDE` view shows the wall reference line, but its `FRONT` view must not (a wall line on a face-on view reads as confusing, not helpful — already true in the old spec, but now structurally enforced rather than coincidental, since both views share the *same* `SkeletonPose`).

`RoutineExercisePoses.targetPoseViewsFor(exerciseId: String): List<BodyPoseView>` keeps its exact signature and behavior contract from the prior spec (empty list for unknown ids; `wall_push` returns `[SIDE, FRONT]`; every other known id returns one view, `SIDE` except `single_leg_balance_hold` which is `FRONT`) — only the `BodyPoseView.pose` type changes, from a 20-point `BodyPose` to a dozen-float `SkeletonPose`.

Per-exercise angle intent (exact degree values are worked out in the implementation plan, not here — same treatment the point-coordinates got in the prior spec):

- **`wall_push` (SIDE)** — `rootLeanDegrees` ≈ 25–30° (leaning into the wall), hip flexion set to roughly compensate so legs still read as planted, shoulder flexion to raise arms forward to wall height, elbow flexion small (arms mostly extended, slightly bent). **(FRONT)** — same `SkeletonPose`, projected face-on: shoulders/hands/feet all show as genuinely symmetric (`leftShoulder`/`rightShoulder` etc. are set to the same flexion/abduction values), which is the whole payoff of this redesign — zero extra authoring for the second view.
- **`hollow_body_hold`** — negative-ish root lean or a spine curl approximation (lying near the floor is modeled as a large root lean, ~90°, so "up" in the pose becomes "along the floor" after projection — see §7 on fitting/centering, this works because the renderer doesn't assume a fixed "standing" orientation), shoulder flexion to raise arms overhead, hip flexion to lift legs.
- **`calf_raise_hold`** — all angles nominal/near-zero (standing tall), the heel/toe fork is what will (still) do the visual work of suggesting a raise is possible in a future revision if we ever give ankle its own DOF; for this pass it's visually close to neutral standing, same accepted limitation shape as the prior spec, just for a different structural reason (ankle has no DOF yet, by design, not because heel/toe points didn't exist).
- **`wall_sit`** — root lean ~0 (back vertical against wall), hip flexion ~90° (thigh horizontal), knee flexion ~90° (shin vertical) — the "invisible chair" L-shape, now literally two hinge angles instead of four hand-placed coordinates.
- **`single_leg_balance_hold`** — standing leg near-neutral; lifted leg's hip given both flexion (forward) and a knee bend, giving genuine 3D difference between the two legs' angles (this pose already needed asymmetry in the old model via a 3/4-front layout; here it's just two different `JointRotation` values, and it now renders sensibly from *any* requested view instead of only the one it was hand-drawn for).
- **`reverse_table_hold`** — root lean negative-ish (reclined), shoulder extension (arms behind, supporting on the floor), hip/knee flexion for bent legs, feet flat.
- **`plank_hold`** — root lean ~90° (horizontal body), shoulder flexion to bring forearms under shoulders, hips/knees near straight.

## 7. Rendering (`ui/workout/BodyPoseIllustration.kt`)

Composable signature is unchanged in spirit: `BodyPoseIllustration(pose: SkeletonPose?, angle: PoseViewAngle, modifier, progressToTarget, accessibilityDescription)` — `pose` replaces the old `targetPose: BodyPose?`, and `angle` is new (the old renderer had no notion of "which view," since a `BodyPose` fully baked in its view already).

Steps inside the composable:
1. `val resolved = resolve(if (progressToTarget < 1f) interpolatePose(neutralPose, pose ?: neutralPose, progressToTarget) else pose ?: neutralPose)` — interpolation now happens in **angle space** (§8), not point space.
2. `val projected = project(resolved, angle)`
3. **Fit to canvas:** compute the bounding box (min/max x, min/max y) across `projected.values`, then scale uniformly (preserving aspect ratio) and translate so the figure is centered with a small margin inside the actual `Canvas` `size`. This replaces the old model's implicit "everything is pre-normalized to 0f..1f" assumption — poses are no longer authored in canvas-fraction space at all, so the renderer must fit them dynamically. This is a genuinely new responsibility for the renderer, called out explicitly since it wasn't needed before.
4. Draw, same visual primitives as before: head circle, spine line (`Neck`→`SpineTop`... →`Pelvis`, or simplified to a single `SpineTop`→`Pelvis` segment — no intermediate spine curve, matching "simplified, don't overbuild"), **shoulder line** (`LeftShoulder`–`RightShoulder`, always drawn — design goal 2), **pelvis line** (`LeftHip`–`RightHip`, always drawn — design goal 2), arm polylines (`Shoulder→Elbow→Wrist→Hand`), leg polylines (`Hip→Knee→Ankle`) plus the heel/toe fork (`Ankle→Heel`, `Ankle→Toe`), joint dots, and the optional wall/floor `prop` line (from `BodyPoseView.prop`, §6).

Goal 2 ("shoulder line and pelvis line should always be visible") is now structurally guaranteed — they're drawn unconditionally every frame, not dependent on any per-pose data being present, since `LeftShoulder`/`RightShoulder`/`LeftHip`/`RightHip` are always resolved by FK for every pose.

## 8. Interpolation

```kotlin
fun interpolatePose(from: SkeletonPose, to: SkeletonPose, progress: Float): SkeletonPose
```

Lerps every float field (each `JointRotation`'s `flexionDegrees`/`abductionDegrees`, each bare-Float knee/elbow angle, and `rootLeanDegrees`) independently, `progress` clamped `0f..1f`, matching the old `interpolatePose`'s clamping behavior. This is simpler than the old point-space lerp (scalar float lerps vs. `Offset` lerps) and more anatomically plausible in-between frames — joints rotate smoothly toward their target angle rather than points sliding through space in straight lines (which could make a limb appear to stretch or pass through the body mid-animation in the old model). `poseProgressFor(phase, secondsRemaining): Float` (mapping `WorkoutPhase` to the 0f..1f progress target) is **unchanged** — it has no dependency on the pose representation.

## 9. Package layout

```
workout/pose/skeleton/Vec3.kt              — Vec3 + plus()
workout/pose/skeleton/JointRotation.kt     — JointRotation
workout/pose/skeleton/SkeletonPose.kt      — SkeletonPose, SkeletonJoint enum
workout/pose/skeleton/SkeletonProportions.kt — the cranial-unit constants from §3
workout/pose/skeleton/ForwardKinematics.kt — resolve()
workout/pose/skeleton/ProjectedPoint.kt    — ProjectedPoint
workout/pose/skeleton/PoseProjector.kt     — project()
workout/pose/skeleton/PoseInterpolation.kt — interpolatePose() (angle-space)

workout/pose/PoseViewAngle.kt              — SIDE/FRONT/BACK
workout/pose/PoseProp.kt                   — NONE/WALL/FLOOR (unchanged from prior spec)
workout/pose/BodyPoseView.kt               — BodyPoseView (angle, label, pose, prop)
workout/pose/RoutineExercisePoses.kt       — neutral SkeletonPose + 7 exercises' angle-based poses; targetPoseViewsFor()
workout/pose/PoseProgress.kt               — poseProgressFor() (unchanged)

ui/workout/BodyPoseIllustration.kt         — resolve + project + fit-to-canvas + draw
```

`workout/pose/skeleton` holds the pure-Kotlin FK/projection engine (testable with zero Compose); `workout/pose` holds the app-facing catalog and view/prop concepts, matching the split already established for `workout/audio` (pure generator vs. the one Android-dependent file).

**Fully removed, no compat shim:** `BodyPoint.kt`, the old `BodyPose.kt` (points-map version — its `PoseProp` enum is kept but moves to its own file), the ~180 hand-authored coordinate literals across all 9 old poses in `RoutineExercisePoses.kt`. Consistent with this project's established no-backwards-compat-hack convention.

## 10. Testing

- `ForwardKinematicsTest` — resolve a known simple `SkeletonPose` (e.g. the neutral pose) and assert exact expected `Vec3` positions for representative joints (shoulders at `±shoulderHalfWidth` on x, elbow directly below shoulder by `upperArmLength` when flexion/abduction are both 0, etc.); resolve a pose with a known non-zero flexion and assert the resulting position matches hand-computed trigonometry for that one joint; assert `resolve(...).keys == SkeletonJoint.entries.toSet()` for at least one pose, guarding the by-construction completeness pattern described in the `RoutineExercisePosesTest` bullet below.
- `PoseProjectorTest` — given known 3D joint positions, assert `SIDE` drops x, `FRONT` drops z, `BACK` drops z and negates x.
- `PoseInterpolationTest` — progress 0 returns `from`'s angles, 1 returns `to`'s angles, 0.5 returns the midpoint, for a representative field; clamping above 1 / below 0, matching the old test's shape.
- `RoutineExercisePosesTest` — every known id's `targetPoseViewsFor` returns a non-empty list; `wall_push` returns exactly `[SIDE "Side", FRONT "Front"]`; every other known id returns exactly one view; unknown id returns an empty list. No "all N points present" test is needed for the *catalog* poses specifically — unlike the old model, a `SkeletonPose`'s fields are fixed named properties (not a `Map` an author could forget to populate), so there's no per-pose "missing point" failure mode to guard against there. `ForwardKinematicsTest` (above) is what actually guards `resolve`'s output completeness, by implementing it via `SkeletonJoint.entries.associateWith { ... }` (same by-construction pattern the old `interpolatePose` used for `BodyPoint`) and asserting the resolved map's key set equals `SkeletonJoint.entries.toSet()`.
- No Canvas rendering test — same established precedent, verified by compiling, `./gradlew build`, and manual on-device testing across all three view angles.

## 11. Out of scope (explicit)

Torso twist, sideways (frontal-plane) root lean, independent wrist/ankle rotation, automatic "feet stay grounded" inverse kinematics, any 3D graphics library/engine/rotation-matrix or quaternion math, arbitrary 3-axis rotation on every joint, a spine curve (spine renders as one straight segment), camera input, ML Kit, Rive, Lottie, Spine, PICO/XR, changes to `workout/audio/**` or any TextToSpeech behavior.

## 12. Commits

```
docs(spec): design skeleton armature pose model
feat(workout): add skeleton proportions and forward kinematics
feat(workout): add pose projection and angle-based interpolation
feat(workout): redefine routine exercise poses as joint angles
feat(ui): render skeleton poses via resolve-project-fit pipeline
```

## 13. Acceptance

```bash
./gradlew test
./gradlew build
```

Manual, on-device:
- shoulder line and pelvis line are visibly present in every phase of every exercise, including the neutral/rest pose
- `wall_push`'s Side and Front views both look correct and are derived from visibly the same pose (symmetric limbs in Front, clear lean in Side)
- hands and feet render as visible short segments, not just line endpoints
- all 7 exercises still render recognizably, across the countdown-to-target animation
- no crash; portrait/landscape/rotation behavior from the prior spec is unaffected (this spec doesn't touch layout code)
