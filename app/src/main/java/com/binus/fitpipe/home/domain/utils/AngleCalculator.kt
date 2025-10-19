package com.binus.fitpipe.home.domain.utils

import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import kotlin.math.acos
import kotlin.math.sqrt

object AngleCalculator {
    private const val ANGLE_TOLERANCE = 40f

    /**
     * Calculates the angle between three points, where p2 is the vertex of the angle.
     * @param p1 First point
     * @param p2 Vertex point (where the angle is measured)
     * @param p3 Third point
     * @return Angle in degrees
     */
    fun getAngleBetweenPoints(
        p1: Float2,
        p2: Float2,
        p3: Float2,
    ): Float {
        val baX = p1.x - p2.x
        val baY = p1.y - p2.y
        val bcX = p3.x - p2.x
        val bcY = p3.y - p2.y

        // Dot product and magnitudes
        val dot = baX * bcX + baY * bcY
        val magBA = sqrt(baX * baX + baY * baY)
        val magBC = sqrt(bcX * bcX + bcY * bcY)

        if (magBA == 0f || magBC == 0f) return 0f // avoid division by zero

        // cos(theta)
        val cosTheta = (dot / (magBA * magBC)).coerceIn(-1f, 1f)

        // Convert to degrees
        return Math.toDegrees(acos(cosTheta).toDouble()).toFloat()
    }

    fun get3dAngleBetweenPoints(
        p1: Float3,
        p2: Float3,
        p3: Float3,
    ): Float {
        // Create vectors from p2 to p1 and p2 to p3
        val baX = p1.x - p2.x
        val baY = p1.y - p2.y
        val baZ = p1.z - p2.z

        val bcX = p3.x - p2.x
        val bcY = p3.y - p2.y
        val bcZ = p3.z - p2.z

        // Dot product
        val dot = baX * bcX + baY * bcY + baZ * bcZ

        // Magnitudes
        val magBA = sqrt(baX * baX + baY * baY + baZ * baZ)
        val magBC = sqrt(bcX * bcX + bcY * bcY + bcZ * bcZ)

        if (magBA == 0f || magBC == 0f) return 0f // avoid division by zero

        // cos(theta)
        val cosTheta = (dot / (magBA * magBC)).coerceIn(-1f, 1f)

        // Convert to degrees
        return Math.toDegrees(acos(cosTheta).toDouble()).toFloat()
    }

    /**
     * Checks if an angle is within tolerance of an ideal angle.
     * @param actualAngle The measured angle
     * @param idealAngle The target angle
     * @param tolerance The allowed tolerance (default is 20 degrees)
     * @return True if the angle is within tolerance
     */
    fun Float.isInTolerance(
        idealAngle: Float,
        tolerance: Float = ANGLE_TOLERANCE
    ): Boolean {
        return (this >= idealAngle - tolerance / 2 && this <= idealAngle + tolerance / 2)
    }
}