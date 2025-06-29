package com.binus.fitpipe.home.ui

import com.binus.fitpipe.poselandmarker.ConvertedLandmarkList
import retrofit2.http.POST
import retrofit2.http.Path

interface HomeService {
    @POST("endpoint/pose_landmark")
    suspend fun sendPoseLandmark(
        @Path ("posture") poseKey: String,
        @Path("mediapipe") convertedLandmarkList: List<Float>
    ): Result<String>
}