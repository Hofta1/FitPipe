package com.binus.fitpipe.home.ui

import com.binus.fitpipe.home.data.MediaPipeScanResponse
import com.binus.fitpipe.poselandmarker.ExerciseRequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface HomeService {
    @POST("pose")
    suspend fun sendPoseLandmark(
        @Body exerciseRequestBody: ExerciseRequestBody,
    ): MediaPipeScanResponse
}
