package com.binus.fitpipe.home.domain.checker

abstract class ExerciseChecker {
    var isFormOkay = false
    var statusString = "Failed"
    var badFormFrameCount = 0
    val BAD_FORM_THRESHOLD = 12

    fun getFormattedStatus(): String{
        return statusString
    }

    fun getFormStatus(): Boolean {
        return isFormOkay
    }
}