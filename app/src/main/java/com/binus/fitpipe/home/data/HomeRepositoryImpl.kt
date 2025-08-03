package com.binus.fitpipe.home.data

import com.binus.fitpipe.home.ui.HomeService
import com.binus.fitpipe.poselandmarker.ConvertedLandmarkList
import javax.inject.Inject

class HomeRepositoryImpl
    @Inject
    constructor(
        private val service: HomeService,
    ) : HomeRepository {
        override suspend fun sendPoseLandmark(convertedLandmarkList: ConvertedLandmarkList): Result<String> {
            return try {
                println("Sending pose landmark: $convertedLandmarkList")
                service.sendPoseLandmark(convertedLandmarkList)
                Result.success(String())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
