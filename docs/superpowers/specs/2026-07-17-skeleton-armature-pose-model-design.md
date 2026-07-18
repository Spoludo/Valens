# Skeleton/Armature Pose Model — Design

**Status:** Approved
**Scope:** Replace the workout screen's flat, hand-authored 2D point-map pose model (`BodyPoint`/`BodyPose`/`RoutineExercisePoses`, from `docs/superpowers/specs/2026-07-14-workout-guidance-design.md` §3–§8) with a small articulated skeleton/armature model: one set of joint angles per exercise, resolved via lightweight forward kinematics into 3D-ish joint positions, then projected into side/front/back 2D views. This supersedes Part A of the prior spec entirely. **Part B of the prior spec (spoken TextToSpeech cues, §9–§14) is unaffected and stays as documented there** — this spec does not touch `workout/audio/**`.

**Revision (2026-07-17b):** two corrections requested before implementation planning: (1) a minimal single-DOF ankle/foot pitch per side, since `calf_raise_hold` needs to visibly show a raised heel and a fully-fixed ankle couldn't do that; (2) an explicit, unambiguous "angle sign conventions" section (§5, new) — the FK formulas in §6 were re-derived carefully against it, and two sign errors were caught and fixed in the process (documented inline in §5), which is exactly the kind of mistake this section exists to prevent implementers from repeating by trial and error.

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

**Why the foot is a fork, not a continuing bone (unlike the hand):** the reference gives *two* measurements from the ankle — heel (backward) and toe (forward) — because a standing foot genuinely extends both behind and in front of the ankle. A hand has no equivalent "behind the wrist" extent worth modeling. So arms render as a clean 3-segment polyline (shoulder→elbow→wrist→hand-tip); legs render as a 2-segment polyline to the ankle (hip→knee→ankle) **plus** a 2-segment fork at the ankle (ankle→heel-tip, ankle→toe-tip), now steerable by one foot-pitch angle per side (§4, §6). This is a deliberate asymmetry between arms and legs, not an oversight — documenting it here so it doesn't read as inconsistent later.

These are proportional *references* for a readable, legible armature — not a claim of sculptural/anatomical precision. "Good enough to represent exercise positions clearly" is the bar, per the original ask.

## 4. Core types (`workout/pose/skeleton/`)

```kotlin
data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
}
```

Axis convention: `+x` = viewer's right (when facing the figure), `+y` = up, `+z` = toward the viewer (out of the front of the figure — e.g. the direction `wall_push`'s hands reach toward). All three axes are in cranial units. Full sign/rotation conventions are in §5 — read that section before implementing FK (§6).

```kotlin
data class JointRotation(
    val flexionDegrees: Float = 0f,   // bend in the sagittal (side-view) plane
    val abductionDegrees: Float = 0f, // spread in the frontal (front-view) plane — ignored for hinge joints
)
```

Every **limb** bone (upper arm, forearm, hand, thigh, shin) has a rest direction of straight down (`Vec3(0f, -1f, 0f)`) — a relaxed arm/leg hangs down. Elbow/knee only ever set `flexionDegrees` (their `abductionDegrees` stays 0 — enforced by giving those joints no abduction parameter at the `SkeletonPose` call-site level, not by a runtime check, so a misuse is a compile error, not a silent no-op).

The **spine** (and neck/head, which continue the same line) is the opposite case: its rest direction is straight *up* (`Vec3(0f, 1f, 0f)`) — an upright torso stands up from the pelvis. `rootLeanDegrees` tilts this the same way limb flexion does (see §5 for the exact, sign-verified formula — it is **not** the same formula applied naively, because "up" and "down" respond oppositely to the same rotation and one of them needs a sign flip; §5 works through this explicitly).

```kotlin
data class SkeletonPose(
    val rootLeanDegrees: Float = 0f,             // whole-body lean, sagittal plane only
    val leftShoulder: JointRotation = JointRotation(),
    val rightShoulder: JointRotation = JointRotation(),
    val leftElbowDegrees: Float = 0f,             // flexion-only joints: a bare Float, not a JointRotation
    val rightElbowDegrees: Float = 0f,
    val leftHip: JointRotation = JointRotation(),
    val rightHip: JointRotation = JointRotation(),
    val leftKneeDegrees: Float = 0f,
    val rightKneeDegrees: Float = 0f,
    val leftFootPitchDegrees: Float = 0f,         // new: minimal single-DOF ankle pitch, see §5/§6
    val rightFootPitchDegrees: Float = 0f,
)
```

The **neutral pose is `SkeletonPose()`** — every angle defaulting to zero — replacing the old model's hand-authored 20-point `neutralStandingPose`. Wrist has no entry at all (fixed continuation of the forearm direction — "simple for now," unchanged). **Ankle now has one minimal DOF** — `leftFootPitchDegrees`/`rightFootPitchDegrees` — added specifically because a fully-fixed ankle couldn't make `calf_raise_hold` (part of the current beginner routine) show a raised heel, which is not acceptable. This is a single sagittal-plane pitch angle per side, not a `JointRotation` (no abduction component — a foot doesn't need to spread sideways for any exercise in scope) and not full ankle rotation (still "simple," just no longer *zero*).

**Root lean and the legs — corrected while deriving the exact FK formulas (§5/§6):** `rootLeanDegrees` tilts the *spine* (and everything built on it: shoulders, arms, head) — it does **not** tilt the hip offsets or the legs. `LeftHip`/`RightHip` are lateral offsets from the fixed-origin `Pelvis` point, and each leg's own direction comes entirely from its own `JointRotation`/knee-flexion, independent of `rootLeanDegrees`. This is a deliberate simplification, and it means a pose author never needs to "compensate" hip flexion against root lean to keep legs looking planted — a pose with `rootLeanDegrees` set and hip flexion left at its default (0°) already renders with straight, vertical legs, regardless of how much the torso leans. (An earlier draft of this paragraph said the opposite — that hip flexion must counteract root lean, "roughly the negative" of it. That was written before the exact FK formulas were derived and hand-verified in §5, and turned out not to match the simpler behavior those formulas actually produce, once worked through precisely rather than assumed. Corrected here; §7's `wall_push` description is corrected to match.)

## 5. Angle sign conventions

This section is binding on the FK implementation in §6 — if the two ever disagree, this section is correct and §6 has a bug. Every formula below was hand-verified (not just written down) while drafting this revision; two sign mistakes were caught in that process and are called out where they'd naturally occur, specifically because getting this wrong is easy and exactly what this section exists to prevent.

**Coordinate system** (restating §4 precisely):
- `x` = left/right body axis. `+x` = viewer's right.
- `y` = vertical axis. `+y` = up.
- `z` = forward/back depth axis. `+z` = toward the front of the body (the direction a person faces, and the direction `wall_push`'s hands reach toward).

**Projection** (§6 detail, restated here since sign conventions and projection are two views of the same contract):
- `SIDE` drops `x`.
- `FRONT` drops `z`.
- `BACK` drops `z` and mirrors `x` (negates it).

**Rotation helpers.** Two small helpers, each rotating a `Vec3` within one plane:

```kotlin
private fun sagittalRotate(v: Vec3, degrees: Float): Vec3 {
    val t = Math.toRadians(degrees.toDouble())
    val cos = cos(t).toFloat(); val sin = sin(t).toFloat()
    return Vec3(v.x, v.y * cos + v.z * sin, v.z * cos - v.y * sin)
}

private fun frontalRotate(v: Vec3, degrees: Float): Vec3 {
    val t = Math.toRadians(degrees.toDouble())
    val cos = cos(t).toFloat(); val sin = sin(t).toFloat()
    return Vec3(v.x * cos - v.y * sin, v.x * sin + v.y * cos, v.z)
}
```

**Positive angles, and exactly how each formula uses the helpers above:**

- **Root lean:** spine direction = `sagittalRotate(Vec3(0f, 1f, 0f), -rootLeanDegrees)`. **Note the negation.** Applying `sagittalRotate` to the *up* rest vector with a positive, non-negated angle moves it toward `-z` (backward) — the opposite of "positive lean = forward." (This is the first of the two sign mistakes caught while drafting this section: the first version of this formula omitted the negation.) With the negation, positive `rootLeanDegrees` leans the torso forward toward `+z`, as required.
- **Shoulder/hip flexion:** limb base direction = `sagittalRotate(Vec3(0f, -1f, 0f), flexionDegrees)` — **no negation**, applied directly to the *down* rest vector. Positive flexion raises the arm/thigh forward toward `+z`.
- **Shoulder/hip abduction:** applied via `frontalRotate` to the flexion result, but the *sign of the angle passed in* depends on side: `frontalRotate(flexed, abductionDegrees * sideSign)` where `sideSign = -1f` for `Left*` joints and `+1f` for `Right*` joints. Without this per-side flip, the same positive `abductionDegrees` value would move the right limb outward (`+x`, correct) but the left limb *inward* (`+x`, wrong — inward for a joint anchored on the `-x` side). With the flip, `JointRotation.abductionDegrees` always means "outward from the body midline," on both sides, matching the required behavior — the resolver owns the per-side sign, pose authors never think about it.
- **Elbow/knee flexion:** a true hinge, composed **onto the parent bone's already-resolved direction**, not onto a fixed rest vector: forearm direction = `sagittalRotate(upperArmDirection, elbowFlexionDegrees)`; shin direction = `sagittalRotate(thighDirection, kneeFlexionDegrees)`. No negation. This keeps the forearm/shin naturally continuing from wherever the upper arm/thigh actually ended up (including any shoulder/hip flexion+abduction already applied), rather than swinging independently in world space — the difference between a real hinge and an oddly-detached second bone.
- **Foot pitch:** toe direction = `sagittalRotate(Vec3(0f, 0f, 1f), -footPitchDegrees)` — **note the negation**, applied to the *forward* rest vector (a flat foot points forward from the ankle, unlike a limb which rests down). This is the second sign mistake caught while drafting: the un-negated version raises the *toe* at positive pitch instead of the heel — backward from the requirement. With the negation: at `footPitchDegrees = 0`, toe direction is exactly `(0,0,1)` (flat foot, unchanged from before this revision); as `footPitchDegrees` increases, the toe direction's `y` goes *negative* (toe presses toward/into the floor) while `HeelTip = Ankle - toeDirection * heelLength` correspondingly rises (`y` positive — heel lifts). This is deliberately **not** a hinge relative to the shin (unlike elbow/knee) — the foot's rest orientation (forward) isn't a continuation of the shin's rest orientation (down), so composing onto the shin would need extra handling for no real benefit given every in-scope pose has a known, simple shin orientation. Documented here as a deliberate simplification, not an inconsistency with the elbow/knee hinge treatment.

## 6. Forward kinematics & projection (`workout/pose/skeleton/`)

```kotlin
enum class SkeletonJoint {
    Head, Neck, SpineTop, Pelvis,
    LeftShoulder, RightShoulder, LeftElbow, RightElbow, LeftWrist, RightWrist, LeftHand, RightHand,
    LeftHip, RightHip, LeftKnee, RightKnee, LeftAnkle, RightAnkle,
    LeftHeel, RightHeel, LeftToe, RightToe,
}

fun resolve(pose: SkeletonPose): Map<SkeletonJoint, Vec3>
```

`resolve` walks the hierarchy using exactly the formulas in §5 — this section only fixes *where* each formula is anchored, not the formulas themselves:

- `Pelvis` is the root (origin, `Vec3(0f,0f,0f)`).
- `SpineTop = Pelvis + spineDirection * spineLength` (spine direction per §5's root-lean formula).
- `LeftShoulder = SpineTop + Vec3(-shoulderHalfWidth, 0f, 0f)`, `RightShoulder = SpineTop + Vec3(shoulderHalfWidth, 0f, 0f)` — lateral offsets along the *unrotated* x-axis (the shoulder line doesn't independently rotate; it's carried wherever `SpineTop` ends up).
- `Neck`, `Head` continue further along the same spine direction, up from `SpineTop`.
- Each arm: `LeftElbow = LeftShoulder + limbBase(leftShoulder.flexionDegrees, leftShoulder.abductionDegrees, side = Left) * upperArmLength`, then `LeftWrist = LeftElbow + sagittalRotate(elbowDirection, leftElbowDegrees) * forearmLength`, then `LeftHand = LeftWrist + sameDirection * handLength` (hand is a fixed continuation — no further rotation). Right arm mirrors with `side = Right`.
- Each leg: `LeftHip`/`RightHip` are lateral offsets from `Pelvis` the same way shoulders are offset from `SpineTop`; `LeftKnee = LeftHip + limbBase(leftHip.flexionDegrees, leftHip.abductionDegrees, side = Left) * thighLength`; `LeftAnkle = LeftKnee + sagittalRotate(thighDirection, leftKneeDegrees) * shinLength`.
- Each foot: `LeftToe = LeftAnkle + toeDirection(leftFootPitchDegrees) * toeLength`, `LeftHeel = LeftAnkle - toeDirection(leftFootPitchDegrees) * heelLength`, per §5's foot-pitch formula. Right foot mirrors.

22 joints resolved per pose (`SkeletonJoint` has 22 entries — vs. the old model's 20 raw points; similar order of magnitude, now fully derived rather than hand-placed).

```kotlin
data class ProjectedPoint(val x: Float, val y: Float)

fun project(joints: Map<SkeletonJoint, Vec3>, angle: PoseViewAngle): Map<SkeletonJoint, ProjectedPoint>
```

- `SIDE`: `(z, -y)` per joint (drops x; y negated because cranial-unit "up" is `+y` but screen-space "up" is smaller values).
- `FRONT`: `(x, -y)` (drops z).
- `BACK`: `(-x, -y)` (drops z, mirrored — viewing from behind flips apparent left/right).

Output is still in cranial units, not yet fitted to a Canvas — see §8.

## 7. `PoseViewAngle`, `BodyPoseView`, and the pose catalog (`workout/pose/`)

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

- **`wall_push` (SIDE)** — `rootLeanDegrees` ≈ 25–30° (leaning into the wall); hip flexion is left at/near its default (0°) — legs stay planted and vertical automatically, since hip angles are independent of root lean (§4) — shoulder flexion to raise arms forward to wall height, elbow flexion small (arms mostly extended, slightly bent). **(FRONT)** — same `SkeletonPose`, projected face-on: shoulders/hands/feet all show as genuinely symmetric (`leftShoulder`/`rightShoulder` etc. are set to the same flexion/abduction values), which is the whole payoff of this redesign — zero extra authoring for the second view.
- **`hollow_body_hold`** — negative-ish root lean or a spine curl approximation (lying near the floor is modeled as a large root lean, ~90°, so "up" in the pose becomes "along the floor" after projection — see §8 on fitting/centering, this works because the renderer doesn't assume a fixed "standing" orientation), shoulder flexion to raise arms overhead, hip flexion to lift legs.
- **`calf_raise_hold`** — root lean/hip/knee stay near-zero (standing tall), and **both `footPitchDegrees` set to a visible plantarflexion angle (roughly 35–45°)** — heel clearly lifted, toe/contact point staying down. This is the concrete fix for the requirement that motivated §4/§5's foot-pitch addition; it replaces the previous revision's "accepted limitation" note for this exercise entirely.
- **`wall_sit`** — root lean ~0 (back vertical against wall), hip flexion ~90° (thigh horizontal), knee flexion ~90° (shin vertical) — the "invisible chair" L-shape, now literally two hinge angles instead of four hand-placed coordinates. Feet stay flat (`footPitchDegrees` at or near the default 0).
- **`single_leg_balance_hold`** — standing leg near-neutral; lifted leg's hip given both flexion (forward) and a knee bend, giving genuine 3D difference between the two legs' angles (this pose already needed asymmetry in the old model via a 3/4-front layout; here it's just two different `JointRotation` values, and it now renders sensibly from *any* requested view instead of only the one it was hand-drawn for).
- **`reverse_table_hold`** — root lean negative-ish (reclined), shoulder extension (arms behind, supporting on the floor), hip/knee flexion for bent legs. Feet stay flat (`footPitchDegrees` at or near the default 0), per the requirement that this exercise's feet read as flat on the floor.
- **`plank_hold`** — root lean ~90° (horizontal body), shoulder flexion to bring forearms under shoulders, hips/knees near straight. Both `footPitchDegrees` may be set to a small positive value so the toes angle into the floor (matching a real plank's curled-under toes/raised-heel foot position) — optional polish, not required for the pose to read correctly, and left as a specific implementation-time judgment call rather than a fixed number here.

## 8. Rendering (`ui/workout/BodyPoseIllustration.kt`)

Composable signature is unchanged in spirit: `BodyPoseIllustration(pose: SkeletonPose?, angle: PoseViewAngle, modifier, progressToTarget, accessibilityDescription)` — `pose` replaces the old `targetPose: BodyPose?`, and `angle` is new (the old renderer had no notion of "which view," since a `BodyPose` fully baked in its view already).

Steps inside the composable:
1. `val resolved = resolve(if (progressToTarget < 1f) interpolatePose(neutralPose, pose ?: neutralPose, progressToTarget) else pose ?: neutralPose)` — interpolation now happens in **angle space** (§9), not point space.
2. `val projected = project(resolved, angle)`
3. **Fit to canvas:** compute the bounding box (min/max x, min/max y) across `projected.values`, then scale uniformly (preserving aspect ratio) and translate so the figure is centered with a small margin inside the actual `Canvas` `size`. This replaces the old model's implicit "everything is pre-normalized to 0f..1f" assumption — poses are no longer authored in canvas-fraction space at all, so the renderer must fit them dynamically. This is a genuinely new responsibility for the renderer, called out explicitly since it wasn't needed before.
4. Draw, same visual primitives as before: head circle, spine line (`Neck`→`SpineTop`... →`Pelvis`, or simplified to a single `SpineTop`→`Pelvis` segment — no intermediate spine curve, matching "simplified, don't overbuild"), **shoulder line** (`LeftShoulder`–`RightShoulder`, always drawn — design goal 2), **pelvis line** (`LeftHip`–`RightHip`, always drawn — design goal 2), arm polylines (`Shoulder→Elbow→Wrist→Hand`), leg polylines (`Hip→Knee→Ankle`) plus the heel/toe fork (`Ankle→Heel`, `Ankle→Toe`, now pitch-steerable per §5/§6), joint dots, and the optional wall/floor `prop` line (from `BodyPoseView.prop`, §7).

Goal 2 ("shoulder line and pelvis line should always be visible") is now structurally guaranteed — they're drawn unconditionally every frame, not dependent on any per-pose data being present, since `LeftShoulder`/`RightShoulder`/`LeftHip`/`RightHip` are always resolved by FK for every pose.

## 9. Interpolation

```kotlin
fun interpolatePose(from: SkeletonPose, to: SkeletonPose, progress: Float): SkeletonPose
```

Lerps every float field independently — each `JointRotation`'s `flexionDegrees`/`abductionDegrees`, each bare-Float knee/elbow angle, `rootLeanDegrees`, and (new this revision) `leftFootPitchDegrees`/`rightFootPitchDegrees` — `progress` clamped `0f..1f`, matching the old `interpolatePose`'s clamping behavior. This is simpler than the old point-space lerp (scalar float lerps vs. `Offset` lerps) and more anatomically plausible in-between frames — joints rotate smoothly toward their target angle rather than points sliding through space in straight lines (which could make a limb appear to stretch or pass through the body mid-animation in the old model). `poseProgressFor(phase, secondsRemaining): Float` (mapping `WorkoutPhase` to the 0f..1f progress target) is **unchanged** — it has no dependency on the pose representation.

## 10. Package layout

```
workout/pose/skeleton/Vec3.kt              — Vec3 + plus()
workout/pose/skeleton/JointRotation.kt     — JointRotation
workout/pose/skeleton/SkeletonPose.kt      — SkeletonPose (incl. foot-pitch fields), SkeletonJoint enum
workout/pose/skeleton/SkeletonProportions.kt — the cranial-unit constants from §3
workout/pose/skeleton/ForwardKinematics.kt — resolve(), sagittalRotate()/frontalRotate() helpers (§5, §6)
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

**Fully removed, no compat shim:** `BodyPoint.kt`, the old `BodyPose.kt` (points-map version — its `PoseProp` enum is kept but moves to its own file), the ~180 hand-authored coordinate literals across all 9 old poses in `RoutineExercisePoses.kt`. Consistent with this project's established no-backwards-compat-hack convention. If a temporary shim is genuinely unavoidable mid-implementation (e.g. to keep the build green for one commit while both models briefly coexist), it must be removed before that commit lands — not left in place "for later," per the same convention that already caught and reverted exactly this once during the prior spec's implementation.

## 11. Testing

- `ForwardKinematicsTest`:
  - Resolve the neutral `SkeletonPose()` and assert exact expected `Vec3` positions for representative joints (shoulders at `±shoulderHalfWidth` on x at `SpineTop`'s height, elbow directly below shoulder by `upperArmLength`, ankle directly below hip by `thighLength + shinLength`, etc.).
  - Assert `resolve(...).keys == SkeletonJoint.entries.toSet()` for at least one pose, guarding the by-construction completeness pattern described in the `RoutineExercisePosesTest` bullet below.
  - **Sign-convention proof tests (§5), each asserting on the raw resolved `Vec3`, not the projected/screen output:**
    - a pose with `rightShoulder = JointRotation(flexionDegrees = 90f)` resolves `RightWrist`/`RightHand` with a strictly larger `z` than `RightShoulder` (flexion moves the arm forward).
    - a pose with `rightHip = JointRotation(flexionDegrees = 90f)` resolves `RightKnee`/`RightAnkle` with a strictly larger `z` than `RightHip`.
    - a pose with `rootLeanDegrees = 90f` resolves `SpineTop` with a strictly larger `z` than `Pelvis`.
    - a pose with `rightShoulder = JointRotation(abductionDegrees = 90f)` resolves `RightElbow` with a larger `x` than `RightShoulder` (outward for the right side), and the mirrored `leftShoulder = JointRotation(abductionDegrees = 90f)` resolves `LeftElbow` with a **smaller** `x` than `LeftShoulder` (outward for the left side, opposite sign) — proving the per-side flip in §5 is actually implemented, not just described.
    - a pose with `rightFootPitchDegrees = 45f` resolves `RightHeel` with a strictly larger `y` than `RightToe` (heel lifted relative to toe).
- `PoseProjectorTest` — given known 3D joint positions: `SIDE` drops x (screen `x` equals input `z`); `FRONT` drops z (screen `x` equals input `x`); `BACK` drops z and screen `x` equals the *negation* of input `x` (mirrors).
- `PoseInterpolationTest` — progress 0 returns `from`'s angles, 1 returns `to`'s angles, 0.5 returns the midpoint, for a representative field including one of the new foot-pitch fields; clamping above 1 / below 0, matching the old test's shape.
- `RoutineExercisePosesTest` — every known id's `targetPoseViewsFor` returns a non-empty list; `wall_push` returns exactly `[SIDE "Side", FRONT "Front"]`; every other known id returns exactly one view; unknown id returns an empty list; `calf_raise_hold`'s pose has a non-zero `leftFootPitchDegrees`/`rightFootPitchDegrees` (regression guard for the exact requirement that motivated this revision). No "all N points present" test is needed for the *catalog* poses specifically — unlike the old model, a `SkeletonPose`'s fields are fixed named properties (not a `Map` an author could forget to populate), so there's no per-pose "missing point" failure mode to guard against there.
- No Canvas rendering test — same established precedent, verified by compiling, `./gradlew build`, and manual on-device testing across all three view angles.

## 12. Out of scope (explicit)

Torso twist, sideways (frontal-plane) root lean, full ankle 3D rotation (only the single sagittal pitch angle from §4/§5), full wrist rotation (still fixed), automatic "feet stay grounded" inverse kinematics, physics, mesh/3D rendering, any 3D graphics library/engine/rotation-matrix or quaternion math, arbitrary 3-axis rotation on every joint, a spine curve (spine renders as one straight segment), camera input, ML Kit, Rive, Lottie, Spine, PICO/XR, changes to `workout/audio/**` or any TextToSpeech behavior.

## 13. Commits

```
docs(spec): design skeleton armature pose model
feat(workout): add skeleton proportions and forward kinematics
feat(workout): add pose projection and angle-based interpolation
feat(workout): redefine routine exercise poses as joint angles
feat(ui): render skeleton poses via resolve-project-fit pipeline
```

## 14. Acceptance

```bash
./gradlew test
./gradlew build
```

Manual, on-device:
- shoulder line and pelvis line are visibly present in every phase of every exercise, including the neutral/rest pose
- `wall_push`'s Side and Front views both look correct and are derived from visibly the same pose (symmetric limbs in Front, clear lean in Side)
- hands and feet render as visible short segments, not just line endpoints
- `calf_raise_hold` visibly shows a lifted heel with the toe/contact point staying down — the concrete fix this revision was written for
- all 7 exercises still render recognizably, across the countdown-to-target animation
- no crash; portrait/landscape/rotation behavior from the prior spec is unaffected (this spec doesn't touch layout code)

---

## 15. Workout screen usability additions (2026-07-17c)

**Unrelated to the skeleton/armature pose model above** (§1–§14) — bundled into this spec file at explicit request, so all current workout-screen-guidance work lives in one place. Three MVP usability requirements from on-device testing, not polish:

### 15.1 Exercise progress within the routine

The workout screen shows `Set Y/N` for the current exercise's sets, but nothing shows progress through the *routine* itself (which of the 7 exercises is this). `WorkoutUiState.Running` gains two fields:

```kotlin
val currentExerciseNumber: Int  // 1-based, for display
val totalExercises: Int
```

`WorkoutEngineState.exerciseIndex` stays 0-based internally, unchanged — `currentExerciseNumber = state.exerciseIndex + 1` is computed once, in `WorkoutViewModel.toUiState`. `totalExercises = routine.size` (the ViewModel already holds `routine: List<Exercise>`).

Display: portrait shows `"Exercise X/M"` and `"Set Y/N"` as two separate lines (there's room). Landscape combines them into one compact line, `"Exercise X/M · Set Y/N"`, to conserve the vertical space that §7's landscape layout already treats as scarce — consistent with that section's stated priority order (timer and controls outrank secondary text, and the illustration is what shrinks first, not this).

### 15.2 Phase progress bar

A non-interactive `LinearProgressIndicator` (Compose Material3 — already a dependency, no new library) placed directly below the numeric seconds countdown, in both portrait and landscape. It complements the numeric timer, not replaces it — both stay visible.

```kotlin
fun phaseProgressFor(phase: WorkoutPhase, secondsRemaining: Int, totalPhaseSeconds: Int): Float
```

A pure function (no Compose dependency, unit-testable) living in `ui/workout/` (its only consumer is `WorkoutScreen`, and unlike `poseProgressFor` — which drives actual skeleton-pose interpolation, a domain-adjacent animation input — this is purely a display-formatting concern, so it doesn't belong in `workout/pose/`):

- `COMPLETE` → always `1f`.
- `totalPhaseSeconds <= 0` → always `0f` (defensive: avoids division by zero: "if total duration is unknown or zero, safely return 0f or 1f as appropriate; do not crash" per the request — `COMPLETE` is the one case where `1f` is correct even with a degenerate duration).
- Otherwise → `(1f - secondsRemaining / totalPhaseSeconds.toFloat()).coerceIn(0f, 1f)` — 0 at the start of the phase, approaching 1 as `secondsRemaining` approaches 0, for `COUNTDOWN`, `WORK`, and `REST` alike.

`totalPhaseSeconds` is a **new** `WorkoutUiState.Running` field, computed in the ViewModel per the current phase (not a fixed value) — `PREP_COUNTDOWN_SECONDS` during `COUNTDOWN`, `totalWorkSeconds(exercise)` during `WORK` (reusing the existing shared helper from the audio-cue work), `exercise.defaultPrescription.restSeconds` during `REST`, `0` during `COMPLETE`/if the exercise is somehow unresolved. This keeps `WorkoutEngine` itself untouched and pure — the mapping from phase to "how long is this phase" is computed in the ViewModel from data the engine already exposes (the current exercise and phase), not added to the engine.

### 15.3 Keep screen awake during active workouts

Already specified and planned (implementation plan's `KeepScreenOn` task) — restated here so this requirement is documented in the spec, not just the plan. Scope: keep the screen on during `COUNTDOWN`/`WORK`/`REST`; release on `COMPLETE` or when leaving the workout screen; no `WAKE_LOCK` permission; not Application-global; not active on the Home screen.

```kotlin
@Composable
fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        val previous = view.keepScreenOn
        view.keepScreenOn = enabled || previous
        onDispose { view.keepScreenOn = previous }
    }
}
```

Called from `WorkoutScreen` with `enabled = state is WorkoutUiState.Running && state.phase != WorkoutPhase.COMPLETE`.

### 15.4 Acceptance additions

```bash
./gradlew test
./gradlew build
```

Manual, on-device (in addition to §14's list):
- UI shows `Exercise X/M` (portrait: separate line; landscape: combined with `Set Y/N`)
- UI still shows `Set Y/N`
- the phase progress bar visibly advances during `COUNTDOWN`, `WORK`, and `REST`, and reaches full at `COMPLETE`
- the numeric seconds countdown remains visible alongside the bar
- timer and controls remain visible and reachable in both portrait and landscape with all these additions present
- the screen stays awake during `COUNTDOWN`/`WORK`/`REST` and is free to sleep again after `COMPLETE` or leaving the workout screen; the Home screen is unaffected

---

## 16. Hand pitch and pose-catalog corrections (2026-07-18)

**Unrelated to §15** — a correction pass on-device testing surfaced: several illustrated poses didn't read as their real-world isometric shape (a "hip break" where a leaning body should read as one straight line; legs tucked under the torso instead of extending away from it; a wall-sit whose back wasn't visibly against the wall; a "tabletop" that didn't read as a table; a plank with broken support geometry), and the "hand simply continues the forearm direction" rule (§4/§6, unchanged until now) meant no pose could show a genuine hand-flat-against-a-surface contact — `wall_push`'s palm on the wall, `reverse_table_hold`'s palms on the floor. This is a correction pass on the existing model, **not** a re-architecture: no IK, no new external framework, no change to `workout/audio/**`, no touch to `WorkoutEngine`.

### 16.1 Hand pitch — a new minimal DOF, mirroring foot pitch but hinged, not fixed

`SkeletonPose` gains two more fields, same naming style as foot pitch:

```kotlin
val leftHandPitchDegrees: Float = 0f
val rightHandPitchDegrees: Float = 0f
```

Unlike foot pitch (§4/§5, a **fixed-reference** rotation independent of the shin — a deliberate simplification documented there), hand pitch is a genuine **hinge**, composed onto the forearm's already-resolved direction, exactly the pattern already used for elbow/knee (§5, "a true hinge, composed onto the parent bone's already-resolved direction"):

```kotlin
val handDirection = sagittalRotate(forearmDirection, handPitchDegrees)
val Hand = Wrist + handDirection * handLength
```

No negation — matches the elbow/knee hinge convention exactly (`sagittalRotate(parentDirection, angle)`, direct, no sign flip). At `handPitchDegrees = 0`, `handDirection = forearmDirection` exactly, so every existing pose (all of which default this field to `0f`) renders identically to before this revision — a pure regression guard, not just an assumption.

**Why hinged, not fixed like foot pitch:** the ask was explicitly "the hand segment should rotate *relative to the forearm*" — and unlike the shin (which every in-scope pose keeps at one simple, mostly-vertical orientation, the justification foot pitch's fixed-reference treatment relies on), the forearm's orientation varies a lot across poses in scope (`wall_push`'s reaches forward, `reverse_table_hold`'s hangs straight down) — a fixed world-reference formula couldn't give a controllable "flatten the palm against whatever surface the forearm is reaching toward" result. A hinge can, because it's defined relative to wherever the forearm actually ended up.

**Sign convention** — worked through and verified against the two concrete scenarios the pose catalog actually needs (not asserted as one "universal" rule; a hinge's sign meaning is inherently pose-dependent, the same honest treatment §5 already gives elbow/knee, not a new standard):
- `wall_push`: forearm reaching forward and slightly down (roughly `(0, -0.2, 0.98)`), `handPitchDegrees = +85` rotates the hand to point mostly `+y` (up), with `+z` all but unchanged — a flat palm against a vertical wall in front of the body, fingers pointing up the wall.
- `reverse_table_hold`: forearm hanging straight down (`(0, -1, 0)`), `handPitchDegrees = -90` rotates the hand to exactly `(0, 0, -1)` — flat and horizontal, fingers pointing back toward the knees — a flat palm on the floor behind the torso.

Both proof-tested (§16.2) on the raw resolved `Vec3`, the same way §11's existing sign-proof tests are.

### 16.2 Interpolation, FK, testing

- `interpolatePose` (skeleton package) lerps the two new fields, same treatment as every other field — no special-casing.
- `ForwardKinematicsTest` gains:
  - a pose with `leftHandPitchDegrees = 90f` (arm otherwise neutral) resolves `LeftHand` to a position different from the `handPitchDegrees = 0f` case — hand pitch changes the hand tip relative to the wrist.
  - `handPitchDegrees = 0f` on a pose with a non-trivial forearm direction resolves `Hand` exactly on the ray `Wrist + forearmDirection * handLength` — the explicit regression guard for "unchanged when the new field is left at its default."
  - the `wall_push`-style scenario from §16.1: a forward-and-down forearm with `handPitchDegrees = 85f` resolves a `Hand` with `y` strictly greater than `Wrist`'s `y` (hand tips upward relative to the wrist, wall-contact orientation).
  - the `reverse_table_hold`-style scenario from §16.1: a straight-down forearm with `handPitchDegrees = -90f` resolves a `Hand` with `y` approximately equal to `Wrist`'s `y` (hand goes flat/horizontal) and `z` strictly less than `Wrist`'s `z` (fingers point back toward the body) — floor-contact orientation.
- `PoseInterpolationTest` (skeleton package) gains the two new fields in the existing progress-0/1/0.5 and clamp-above-1/below-0 assertions, the same shape as every other field already covered there.
- Existing foot-pitch tests are unaffected — hand pitch is additive, foot pitch's formula is untouched.

### 16.3 Wall prop placement — near vs. far edge

The two `WALL`-prop exercises face opposite directions relative to the wall: `wall_push` faces the wall (hands reach `+z`, matching the existing convention that `WALL` renders near the high-`z`/far edge of the fitted bounding box, at 90% canvas width); `wall_sit` has its *back* against the wall (the spine sits at `z ≈ 0`, the pose's minimum-`z`/near edge, while the legs extend toward the maximum-`z`/far edge). A single "wall always renders at the far edge" constant cannot be correct for both orientations at once. `BodyPoseView` gains one field:

```kotlin
val propNearEdge: Boolean = false
```

`false` (the default) preserves the existing far-edge (90% width) placement for every current caller — `wall_push`, and every `FLOOR` prop (which has no such ambiguity: the lowest-`y` point is always where the body actually touches the floor, in every exercise in the catalog, so `FLOOR` needs no near/far distinction). `wall_sit` sets `propNearEdge = true`; `BodyPoseIllustration`'s `WALL` drawing branch renders at 10% width instead of 90% when the view's `propNearEdge` is true. This is a **placement-only** change — `wall_sit`'s joint angles are unchanged, matching the finding that "the wall-sit shape itself is mostly good... this is mainly a placement/alignment correction," not a pose redesign.

### 16.4 Pose-catalog corrections

Exact values below were derived algebraically from the FK formulas in §5/§6 (not guessed) and numerically verified against the actual formulas before being written here — the same rigor already applied to the original catalog and to deriving §5 itself.

**A recurring building block — the "collinear extension" identity:** for any `rootLeanDegrees = θ`, setting a limb's own flexion to `-θ` makes that limb's direction exactly `-spineDirection` — i.e., it continues the spine's line through the pelvis/hip, in the opposite direction. (Setting it to `+θ` instead continues the line in the *same* direction as the spine, useful when a limb should extend further the way the torso is already leaning, as in `wall_push`'s legs.) This single identity — proved algebraically via rotation-composition addition (`sagittalRotate` is a rotation matrix; composing two of them adds their angles) — is what fixes the "hip break" issue in `wall_push`, `hollow_body_hold`'s general shape, and `plank_hold`'s straight-line requirement, all at once. It was already implicit in the FK design but not previously exploited by the catalog.

- **`wall_push`** — was folding at the hip (legs stayed vertical, independent of the torso's lean — correct per §4's documented default behavior, but not the shape a real wall push has) and had only a slight elbow bend. Corrected: `rootLeanDegrees = 28f` (unchanged); `leftHip`/`rightHip = JointRotation(flexionDegrees = -28f)` (new — the collinear-extension identity, so the legs continue the torso's lean instead of hanging vertical, removing the hip break); knee stays at its `0f` default (straight legs); `leftShoulder`/`rightShoulder = JointRotation(flexionDegrees = 45f, abductionDegrees = 10f)` (was `78f`/`10f` — lowered so the elbow bend, below, doesn't drop the hand implausibly low); `leftElbowDegrees`/`rightElbowDegrees = 60f` (was `15f` — the requested "more pronounced" bend; **positive**, not negative, because composing a `+60°` bend onto this particular shoulder angle keeps the hand near shoulder height reaching forward, verified numerically — a negative bend here drags the hand down near pelvis height, verified as the wrong choice by trying it first); `leftHandPitchDegrees`/`rightHandPitchDegrees = 85f` (new — §16.1, angles the palm to read as flat against the wall instead of the hand just continuing the forearm's forward-reaching line); `leftFootPitchDegrees`/`rightFootPitchDegrees = 35f` (new — heel visibly lifted, weight on the forefoot, matching `calf_raise_hold`'s established magnitude for the same visual). Front view is unaffected structurally (still the same symmetric `SkeletonPose`, `prop = NONE`) — symmetry was never the issue.

- **`hollow_body_hold`** — legs were tucked underneath the torso rather than extending away from it. Root cause: hip flexion of `+80f` swings the legs *toward* `+z`, the same direction the (already-reclined, `rootLeanDegrees = 90f`) torso itself extends — so torso and legs pointed the same way instead of opposite ways. Corrected: `rootLeanDegrees = 78f` (was `90f` — a few degrees short of fully flat so the torso end is genuinely elevated above the pelvis, not lying exactly flush with it); `leftShoulder`/`rightShoulder = JointRotation(flexionDegrees = 105f)` (was `95f` — raised further so the arms extend past the head continuing the torso's own line, per the collinear-extension identity's same-direction case, `180° − rootLeanDegrees` plus a small excess for "reaching further"); `leftHip`/`rightHip = JointRotation(flexionDegrees = -100f)` (was `+80f` — the sign flip is the actual fix: negative hip flexion past `-90°` swings the legs backward *and* slightly upward, away from the torso, "slightly elevated," per the collinear-extension identity's opposite-direction case). Knee stays at its `0f` default (straight legs, not folded). Verified numerically: pelvis is the single lowest point in the resolved pose (`y = 0`), and both the head end and the ankle end are elevated above it (`y ≈ 1.0–1.9`) while pointing in opposite `z` directions — the classic hollow-body "banana," pelvis/low-back as the only floor contact.

- **`wall_sit`** — joint angles are **unchanged** (`leftHip`/`rightHip = JointRotation(flexionDegrees = 90f)`, `leftKneeDegrees`/`rightKneeDegrees = -90f`) — this shape (torso vertical, thigh horizontal, shin vertical, the "invisible chair" L) was already correct, matching the finding that this is a placement issue, not a pose issue. The actual bug: with this pose, the entire spine sits at `z = 0` (the pose's minimum, since the legs extend forward into positive `z`), but the `WALL` prop always rendered at the *far* (`z`-maximum, 90%-width) edge — placing the drawn wall next to the knees/feet, not the back. Fixed per §16.3: `wall_sit`'s `BodyPoseView` sets `propNearEdge = true`, moving the drawn wall line to the near (`z`-minimum, 10%-width) edge, where the spine actually is.

- **`reverse_table_hold`** — the previous shape (`rootLeanDegrees = -60f`, shoulder extension, bent legs with no particular alignment) didn't read as a tabletop. Corrected using the collinear-extension identity twice — once for the torso/thigh "tabletop surface," once implicitly for the vertical support limbs: `rootLeanDegrees = 82f` (a few degrees short of fully flat, "chest lifted," while staying close to parallel with the floor as requested); `leftHip`/`rightHip = JointRotation(flexionDegrees = -82f)` (= `-rootLeanDegrees` — makes the thigh exactly collinear with the torso, opposite direction, so shoulder–pelvis–knee forms one continuous flat line, the actual "tabletop surface"); `leftKneeDegrees`/`rightKneeDegrees = 82f` (= `+rootLeanDegrees` — brings the shin back to exactly vertical from the now-tilted thigh, so the shin still drops straight to the floor); `leftShoulder`/`rightShoulder = JointRotation()` (was `flexionDegrees = -70f` — reset to the `0f` default, a perfectly vertical upper arm, since the arm needs to drop straight to the floor like a table leg, not extend backward); `leftElbowDegrees`/`rightElbowDegrees = 0f` (was unset/`0f` already — straight arm, confirmed correct, no bend needed for a vertical support limb); `leftHandPitchDegrees`/`rightHandPitchDegrees = -90f` (new — §16.1, rotates the hand from continuing straight down to lying flat on the floor, fingers toward the knees). Foot pitch stays at its `0f` default (flat feet, as required). Verified numerically: the resolved hand and ankle end up within 0.1 cranial units of the same height — both "table legs" reach the same floor, and the shoulder–pelvis–knee line is exactly straight (by construction, via the identity).

- **`plank_hold`** — confirmed against `exercise-packs/bundled/isometric-foundations/exercises/plank_hold.json`: every progression entry (`forearm_30s`/`45s`/`60s`) is tagged `variationKey: "variation.forearm"`; there is no straight-arm progression at all. The intended variant is unambiguously the **forearm plank** — elbows under shoulders, forearms on the floor. This was already the general shape attempted (`leftElbowDegrees = -90f`), but two things were wrong: (1) hip flexion was left at its `0f` default, so the legs stayed vertical while the `rootLeanDegrees = 90f` torso was horizontal — the same hip-break bug as `wall_push`; (2) `shoulderFlexion = 85f` placed the elbow far forward of the shoulder instead of under it, and the resulting forearm direction was nearly vertical, not flat on the floor. Corrected: `rootLeanDegrees = 84f` (was `90f` — see below for why not exactly horizontal); `leftHip`/`rightHip = JointRotation(flexionDegrees = -84f)` (new, = `-rootLeanDegrees` — collinear-extension identity, removes the hip break, one straight line from head to ankle); knee stays at its `0f` default (straight legs, as required); `leftShoulder`/`rightShoulder = JointRotation()` (was `flexionDegrees = 85f` — reset to `0f` default, a perfectly vertical upper arm, putting the elbow exactly under the shoulder — "support clearly under the shoulders," satisfied by construction, not by eyeballing); `leftElbowDegrees`/`rightElbowDegrees = 90f` (was `-90f` — **sign flip**, not just a magnitude change: `+90f` composed onto a now-vertical upper arm gives an exactly horizontal forearm reaching forward, i.e. flat on the floor, matching "forearms on the floor"; the old `-90f` on the old `85f`-flexed upper arm gave a forearm that was nearly vertical again, not flat); `leftFootPitchDegrees`/`rightFootPitchDegrees = 88f` (was `20f` — see below). **Why `rootLeanDegrees = 84f`, not the seemingly-cleaner `90f`:** a perfectly horizontal, perfectly straight body line puts the ankle at the *same height* as the shoulder — but the forearm reaches the floor from `UPPER_ARM_LENGTH` (2.0 units) below the shoulder, while even a maximal foot pitch can only reach `TOE_LENGTH` (1.0 unit) below the ankle, so a perfectly horizontal body line leaves the feet floating roughly 1 unit above where the forearms touch — an "impossible posture" by the letter of the acceptance criteria. Tilting the whole straight line a few degrees off horizontal (still fully collinear, via the same identity, so still "one straight body line") lets the ankle end sit correspondingly lower; `84f` was solved algebraically so the two floor-contact points — resolved forearm and resolved toe — land within 0.01 cranial units of the same height, verified numerically, not eyeballed.

### 16.5 Testing / acceptance addendum

```bash
./gradlew test
./gradlew build
```

Manual, on-device (in addition to §14/§15.4's lists):
- `wall_push` side view reads as an inclined, straight body line from ankle to head, on the toes, with a clearly bent elbow and the hand angled flat against the wall line; front view still reads as symmetric.
- `hollow_body_hold` shows arms and legs extending away from the trunk in opposite directions, both slightly elevated off the floor line — not tucked, not a flat line on the floor.
- `wall_sit`'s drawn wall line sits directly behind the back, not near the knees/feet.
- `reverse_table_hold` reads as a tabletop: torso and thighs form one visibly straight, near-horizontal line; hands and feet both appear to touch the same floor line.
- `plank_hold` reads as a proper forearm plank: one straight body line, forearms flat on the floor with the elbow visibly under the shoulder, no floating gap between the hand-floor-contact and the toe-floor-contact.
- no crash; every other previously-verified exercise (`calf_raise_hold`, `single_leg_balance_hold`) is unaffected — this pass only touches the five exercises above plus the new hand-pitch/prop-placement mechanisms.

### 16.6 On-device correction (2026-07-18b)

First on-device pass on §16.4's values surfaced one real geometric bug and three accepted, non-critical limitations of the fixed-margin fit-to-canvas/prop-placement approach (§8, unchanged).

**The real bug — toe direction disagreeing with leg direction:** `hollow_body_hold` and `reverse_table_hold` both point their legs toward `-z` (the collinear-extension identity's opposite-direction case, §16.4). But `toeDirection` is a *fixed* `+z` reference (§5, unaffected by hand pitch's addition), independent of the leg's own direction — so on both poses the toes visibly pointed back toward the torso instead of continuing away from it, which read as feet in a neutral/resting orientation rather than genuinely planted.

**Fix — mirror the whole pose, don't patch the foot:** rather than special-casing the foot (e.g. an unprincipled `footPitchDegrees = 180f`, which would abuse the pitch parameter for a purpose it doesn't semantically mean), both poses are mirrored across `z` in their entirety — every angle negated (`rootLeanDegrees`, shoulder/hip flexion, knee, hand pitch). Neither pose has an inherent left/right-along-`z` asymmetry (unlike `wall_push`/`wall_sit`, nothing about "the wall" pins down which `z` direction is correct), so a full mirror is a free choice that preserves every previously-verified property (collinearity, matching floor-contact heights — re-verified numerically, unchanged up to sign) while making the legs point toward `+z` — the same direction the fixed toe reference already points. Toes now correctly continue away from the body.

`hollowBodyHoldPose`: `rootLeanDegrees = -78f`; `leftShoulder`/`rightShoulder = JointRotation(flexionDegrees = -105f)`; `leftHip`/`rightHip = JointRotation(flexionDegrees = 100f)`.
`reverseTableHoldPose`: `rootLeanDegrees = -82f`; `leftHip`/`rightHip = JointRotation(flexionDegrees = 82f)`; `leftKneeDegrees`/`rightKneeDegrees = -82f`; `leftHandPitchDegrees`/`rightHandPitchDegrees = 90f` (sign flipped to match the mirrored forearm-to-pelvis direction).

**Accepted, not fixed (explicitly flagged non-critical by the request that reported them):**
- `hollow_body_hold` and `plank_hold` both read as slightly elevated above their drawn floor line. The lowest resolved joint in each pose *is* mathematically at the pose's minimum `y`, matching where `fitToCanvas` + the `FLOOR` prop's fixed 90%-height line should align it — the residual visible gap is `fitToCanvas`'s fixed `0.85f` margin (§8), shared by every pose, not a per-exercise angle bug. Not touched in this pass.
- `wall_sit`'s wall line no longer sits near the knees (§16.3's `propNearEdge` fix confirmed correct on-device) but doesn't touch the spine pixel-perfectly, for the same fixed-margin reason. Not touched in this pass.

### 16.7 Second on-device round (2026-07-18c)

Two more findings, confirmed as: (1) `hollow_body_hold` genuinely is the classic supine (on-the-back) hollow hold — `movementPatternId: "anti_extension_core"` and its rectus/transverse-abdominis-led muscle loads in the exercise pack confirm this, ruling out an initial concern that it might have been mis-modeled as prone; (2) its feet, while now pointing the right way in `z` (§16.6), sat perfectly flat/parallel to the floor line — the requested correction is a foot **flexed slightly upward** (dorsiflexion, toes pulled toward the shin), not the calf-raise-style plantarflexion (toes pressed down) used elsewhere in the catalog.

- `hollowBodyHoldPose` gains `leftFootPitchDegrees`/`rightFootPitchDegrees = -20f` — **negative**, the opposite sign from `calf_raise_hold`'s/`wall_push`'s `+35f`/`+40f`. Verified numerically: at `-20f`, `RightToe.y > RightAnkle.y` (toe lifted above the ankle — flexed upward), the mirror image of the positive-pitch "heel lifted, toe pressed down" case already covered by `resolve_footPitch45_liftsHeelAboveToe`.
- A cosmetic addition, not a pose-accuracy correction: the head gains simple facial detail so the illustrations read less like an anonymous stick figure. `BodyPoseIllustration` draws one hollow (stroked, not filled) eye circle plus a short perpendicular "nose" line for `SIDE` view, and two hollow eye circles for `FRONT` view; `BACK` view draws neither (a face isn't visible from behind). The `SIDE` view's facing direction is inferred cheaply from already-available data — `Head.x >= Pelvis.x` in the fitted/projected points — rather than adding a new head-facing angle to `SkeletonPose` (out of scope; the skeleton model still has no independent head rotation, §12).
