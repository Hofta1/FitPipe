package com.binus.fitpipe.home.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.binus.fitpipe.R
import com.binus.fitpipe.home.data.HomeUiState
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.PoseLandmarkerHelper
import com.binus.fitpipe.ui.theme.Black70
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.binus.fitpipe.ui.theme.Grey70
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.White80
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
internal fun CameraScreen(
    exerciseTitle: String,
    onBackPressed: () -> Unit,
    uiState: HomeUiState,
    onPoseDetected: (exerciseTitle: String, landmarks: List<ConvertedLandmark>) -> Unit,
) {
    FitPipeTheme {
        CameraScreen(
            exerciseTitle = exerciseTitle,
            modifier = Modifier,
            onBackPressed = onBackPressed,
            uiState = uiState,
            onPoseDetected = onPoseDetected,
        )
    }
}

@Composable
private fun CameraScreen(
    exerciseTitle: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    uiState: HomeUiState,
    onPoseDetected: (exerciseTitle: String, landmarks: List<ConvertedLandmark>) -> Unit,
) {
    // Check and request camera permission
    val context = LocalContext.current

    var isRotationEnabled by remember { mutableStateOf(isAutoRotationEnabled(context)) }

    DisposableEffect(context) {
        val contentResolver = context.contentResolver
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                isRotationEnabled = isAutoRotationEnabled(context)
            }
        }

        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            false,
            observer
        )

        onDispose {
            contentResolver.unregisterContentObserver(observer)
        }
    }

    if(isRotationEnabled){
        PoseScanLayoutScreen(
            exerciseTitle = exerciseTitle,
            modifier = modifier,
            onBackPressed = onBackPressed,
            context = context,
            uiState = uiState,
            onPoseDetected = onPoseDetected,
        )
    }else{
        PleaseRotateScreen(modifier = modifier, exerciseTitle = exerciseTitle)
    }
}

@Composable
private fun PoseScanLayoutScreen(
    exerciseTitle: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    context: Context,
    uiState: HomeUiState,
    onPoseDetected: (exerciseTitle: String, landmarks: List<ConvertedLandmark>) -> Unit,
){
    val formattedStatus = uiState.formattedStatusString
    val exerciseCount = uiState.exerciseCount
    val isFormOkay = uiState.isFormOkay

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasCameraPermission = isGranted
        }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Show camera preview only if permission is granted
        if (hasCameraPermission) {
            PoseCameraScreen(exerciseTitle, context, onPoseDetected)
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(550.dp)
                        .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Camera permission required",
                    color = White80,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            Column {
                Row (
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BackButton { onBackPressed() }
                    Text(
                        text = exerciseTitle,
                        style = Typo.BoldTwentyFour,
                        color = White80,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Count: $exerciseCount",
                        style = Typo.MediumEighteen,
                        color = White80,
                        textAlign = TextAlign.Center,
                        modifier = modifier
                            .background(
                                color = Black70,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(8.dp),
                    )
                }
                Row {
                    Box(
                        modifier = modifier.size(20.dp)
                            .background(
                                if (isFormOkay) Color.Green else Color.Red,
                                shape = CircleShape
                            )
                    )
                }
            }
            Text(
                text = formattedStatus,
                style = Typo.MediumEighteen,
                color = White80,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .background(
                        color = Black70,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(start = 8.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(Grey70)
                .size(30.dp)
                .clickable { onBackPressed() },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.line),
            contentDescription = "Back Button",
            modifier =
                modifier
                    .padding(8.dp)
                    .fillMaxSize(),
        )
    }
}

@Composable
fun PoseCameraScreen(
    exerciseTitle: String,
    context: Context,
    onPoseDetected: (String, List<ConvertedLandmark>) -> Unit
) {
    val poseHelper = remember { PoseLandmarkerHelper(context) }

    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val rotation = windowManager.defaultDisplay.rotation

    CameraPreviewView(
        modifier =
            Modifier.fillMaxSize(),
        onPoseDetected = { landmarks ->
            val convertedLandmark = poseHelper.landmarksConverter(landmarks, rotation)
            onPoseDetected(exerciseTitle, convertedLandmark)
        },
        context = context,
        poseHelper = poseHelper,
    )
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onPoseDetected: (landmarks: List<NormalizedLandmark>) -> Unit,
    context: Context,
    poseHelper: PoseLandmarkerHelper,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lastProcessTime = remember { mutableLongStateOf(0L) }

    val controller =
        remember {
            LifecycleCameraController(context).apply {
                setCameraSelector(CameraSelector.DEFAULT_FRONT_CAMERA)
                // Enable both preview and image analysis
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or
                        CameraController.IMAGE_ANALYSIS,
                )
                imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888

                setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(context),
                ) { image: ImageProxy ->
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastProcessTime.value < 100) {
                        // Skip processing if the last frame was processed less than 1 second ago
                        image.close()
                        return@setImageAnalysisAnalyzer
                    }
                    try {
                        val image = image.image
                        image?.let {
                            val result = poseHelper.detect(image, currentTime)
                            result?.landmarks()?.firstOrNull()?.let { landmarks ->
                                onPoseDetected(landmarks)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Error processing image", e)
                    } finally {
                        image.close()
                    }
                }
            }
        }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.unbind()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.controller = controller
                // Set scale type for better preview
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
    )
}

private fun isAutoRotationEnabled(context: Context): Boolean {
    return try {
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION
        ) == 1
    } catch (e: Settings.SettingNotFoundException) {
        false
    }
}

@ComposePreview
@Composable
private fun BackButtonPreview() {
    BackButton(
        modifier = Modifier,
        onBackPressed = {},
    )
}

@ComposePreview
@Composable
private fun CameraScreenPreview() {
    CameraScreen(
        exerciseTitle = "Push Up",
        onBackPressed = {},
        uiState = HomeUiState(),
        onPoseDetected = { _, _ -> },
    )
}

@ComposePreview
@Composable
private fun ScanPosePreview() {
    PoseScanLayoutScreen(
        exerciseTitle = "Push Up",
        onBackPressed = {},
        context = LocalContext.current,
        uiState = HomeUiState(),
        onPoseDetected = { _, _ -> },
    )
}
