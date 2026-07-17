package com.spoludo.valens.workout.pose.skeleton

import com.spoludo.valens.workout.pose.PoseViewAngle

fun project(joints: Map<SkeletonJoint, Vec3>, angle: PoseViewAngle): Map<SkeletonJoint, ProjectedPoint> =
    joints.mapValues { (_, joint) ->
        when (angle) {
            PoseViewAngle.SIDE -> ProjectedPoint(joint.z, -joint.y)
            PoseViewAngle.FRONT -> ProjectedPoint(joint.x, -joint.y)
            PoseViewAngle.BACK -> ProjectedPoint(-joint.x, -joint.y)
        }
    }
