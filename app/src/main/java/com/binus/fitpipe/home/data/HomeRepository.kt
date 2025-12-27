package com.binus.fitpipe.home.data

import com.binus.fitpipe.poselandmarker.ExerciseRequestBody

interface HomeRepository {
    suspend fun sendPoseLandmark(exerciseRequestBody: ExerciseRequestBody): Result<MediaPipeScanResponse>
}
