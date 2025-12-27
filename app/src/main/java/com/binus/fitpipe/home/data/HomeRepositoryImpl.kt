package com.binus.fitpipe.home.data

import com.binus.fitpipe.home.ui.HomeService
import com.binus.fitpipe.poselandmarker.ExerciseRequestBody
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class HomeRepositoryImpl
    @Inject
    constructor(
        private val service: HomeService,
    ) : HomeRepository {
        override suspend fun sendPoseLandmark(exerciseRequestBody: ExerciseRequestBody): Result<MediaPipeScanResponse> {
            return try {
                println("Sending pose landmark: $exerciseRequestBody")
                val data = service.sendPoseLandmark(exerciseRequestBody)
                Result.success(data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

data class MediaPipeScanResponse(
    @SerializedName("status")
    val status: Boolean?,
    @SerializedName("formattedFeedback")
    val formattedFeedback: String?,
    @SerializedName("fullFeedback")
    val fullFeedback: String?,
)
