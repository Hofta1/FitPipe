package com.binus.fitpipe.home.data

import com.binus.fitpipe.home.ui.HomeService
import com.binus.fitpipe.poselandmarker.ConvertedLandmarkList
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class HomeRepositoryImpl
    @Inject
    constructor(
        private val service: HomeService,
    ) : HomeRepository {
        override suspend fun sendPoseLandmark(convertedLandmarkList: ConvertedLandmarkList): Result<MediaPipeScanResponse> {
            return try {
                println("Sending pose landmark: $convertedLandmarkList")
                val data = service.sendPoseLandmark(convertedLandmarkList)
                Result.success(data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

data class MediaPipeScanResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("feedback")
    val feedback: MediaPipeScanFeedback,
)

data class MediaPipeScanFeedback(
    @SerializedName("landmarks")
    val landmarkFeedback: String,
    @SerializedName("angles")
    val angleFeedback: String,
)
