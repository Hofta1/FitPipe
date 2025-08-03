package com.binus.fitpipe.poselandmarker

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(private val context: Context) {
    private var poseLandmarker: PoseLandmarker? = null

    init {
        setupPoseLandmarker()
    }

    private fun setupPoseLandmarker() {
        val baseOptions =
            BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")
                .build()

        val options =
            PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumPoses(1) // Number of poses to detect
                .setMinPoseDetectionConfidence(0.75f) // 50% confidence threshold
                .setMinPosePresenceConfidence(0.75f)
                .setMinTrackingConfidence(1f)
                .setRunningMode(RunningMode.VIDEO)
                .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detect(
        bitmap: Bitmap,
        currentTime: Long,
    ): PoseLandmarkerResult? {
        val mpImage = BitmapImageBuilder(bitmap).build()
        return poseLandmarker?.detectForVideo(mpImage, currentTime)
    }

    fun normalizeLandmarksConverter(landmarks: List<NormalizedLandmark>): List<ConvertedLandmark> {
        val convertedLandmarks =
            landmarks.map { landmark ->
                ConvertedLandmark(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z(),
                    visibility = landmark.visibility(),
                    presence = landmark.presence(),
                )
            }
        return convertedLandmarks
    }
}
