package com.binus.fitpipe.home.data

import com.binus.fitpipe.poselandmarker.ConvertedLandmarkList


interface HomeRepository {
    suspend fun sendPoseLandmark(
        convertedLandmarkList: ConvertedLandmarkList
    ): Result<String>
}