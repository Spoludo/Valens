package com.spoludo.valens.workout.engine

import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.ExerciseId
import com.spoludo.valens.domain.model.ExercisePack

private val FIXED_BEGINNER_ROUTINE_EXERCISE_IDS = listOf(
    "wall_push", "hollow_body_hold", "calf_raise_hold", "wall_sit",
    "single_leg_balance_hold", "reverse_table_hold", "plank_hold",
).map { ExerciseId(it) }

object FixedBeginnerRoutine {
    fun build(pack: ExercisePack): List<Exercise> {
        val byId = pack.exercises.associateBy { it.id }
        return FIXED_BEGINNER_ROUTINE_EXERCISE_IDS.mapNotNull { byId[it] }
    }
}
