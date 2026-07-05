package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ExerciseId(val value: String)

@Serializable
@JvmInline
value class MovementPatternId(val value: String)

@Serializable
@JvmInline
value class JointId(val value: String)

@Serializable
@JvmInline
value class MuscleId(val value: String)

@Serializable
@JvmInline
value class ExerciseFamilyId(val value: String)

@Serializable
@JvmInline
value class CapacityId(val value: String)
