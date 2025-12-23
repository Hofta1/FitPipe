package com.binus.fitpipe.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.binus.fitpipe.R

private val interFamily =
    FontFamily(
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_extrabold, FontWeight.ExtraBold),
        Font(R.font.inter_bold, FontWeight.Bold),
    )

object Typo {
    val MediumTwelve =
        TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
        )
    val ExtraBoldTwentyFour =
        TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
        )
    val MediumSixteen =
        TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
    val BoldTwentyFour =
        TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )
    val BoldTwenty =
        TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    val MediumEighteen =
        TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
        )

    val MediumTwenty =
        TextStyle(
            fontFamily = interFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
        )
}
