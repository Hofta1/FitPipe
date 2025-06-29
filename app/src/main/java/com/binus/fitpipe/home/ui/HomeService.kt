package com.binus.fitpipe.home.ui

import com.binus.fitpipe.poselandmarker.ConvertedLandmarkList
import retrofit2.http.POST

interface HomeService {
    @POST("endpoint/pose_landmark")
    suspend fun sendPoseLandmark(
        convertedLandmarkList: ConvertedLandmarkList
    ): Result<String>
}