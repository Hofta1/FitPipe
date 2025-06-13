package com.binus.fitpipe.home.ui

import androidx.compose.runtime.*
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.binus.fitpipe.poselandmarker.PoseLandmarkerHelper
import com.binus.fitpipe.ui.theme.Black70
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.binus.fitpipe.ui.theme.Grey70
import com.binus.fitpipe.ui.theme.Red50
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.White80
import com.binus.fitpipe.ui.theme.Yellow50
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import java.io.ByteArrayOutputStream
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
internal fun CameraScreen(
    exerciseTitle: String,
    onBackPressed: () -> Unit,
) {
    FitPipeTheme {
        CameraScreen(
            exerciseTitle = exerciseTitle,
            modifier = Modifier,
            onBackPressed = onBackPressed
        )
    }
}

@Composable
private fun CameraScreen(
    exerciseTitle: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    // Check and request camera permission
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (hasCameraPermission == false) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Column {
        Spacer(Modifier.size(75.dp))
        Box(
            modifier = modifier
                .fillMaxWidth()
        ) {
            BackButton { onBackPressed() }
            Text(
                text = exerciseTitle,
                style = Typo.BoldTwentyFour,
                color = White80,
                modifier = modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Show camera preview only if permission is granted
        if (hasCameraPermission) {
            PoseCameraScreen()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera permission required",
                    color = White80,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(16.dp))
                .background(Black70)
                .padding(vertical = 26.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Count: 9",
                style = Typo.MediumEighteen,
                color = White80,
                textAlign = TextAlign.Center,
                modifier = modifier.fillMaxWidth()
            )
            Spacer(modifier.size(14.dp))
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = modifier.height(36.dp)
            ) {
                Text(
                    text = "Feedback Text Long Text Te",
                    style = Typo.BoldTwenty,
                    color = if (true) Yellow50 else Red50, // Replace with actual condition
                    textAlign = TextAlign.Center,
                    modifier = modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Grey70)
            .size(30.dp)
            .clickable { onBackPressed() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.line),
            contentDescription = "Back Button",
            modifier = modifier
                .padding(8.dp)
                .fillMaxSize()
        )
    }
}

@Composable
fun PoseCameraScreen() {
    CameraPreviewView(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp),
        onPoseDetected = { landmarks ->
            Log.d("Pose", "Detected ${landmarks.size} landmarks")
        }
    )
}

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onPoseDetected: (landmarks: List<NormalizedLandmark>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val poseHelper = remember { PoseLandmarkerHelper(context) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA)
            // Enable both preview and image analysis
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.IMAGE_ANALYSIS
            )

            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context)
            ) { image: ImageProxy ->
                try {
                    val bitmap = imageProxyToBitmap(image)
                    val result = poseHelper.detect(bitmap)
                    result?.landmarks()?.firstOrNull()?.let { landmarks ->
                        onPoseDetected(landmarks)
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
        }
    )
}

// Your imageProxyToBitmap function looks correct, but here's a cleaner version:
fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 75, out) // Reduced quality for performance
    val yuv = out.toByteArray()

    return BitmapFactory.decodeByteArray(yuv, 0, yuv.size)
}

@ComposePreview
@Composable
private fun BackButtonPreview() {
    BackButton(
        modifier = Modifier,
        onBackPressed = {}
    )
}

@ComposePreview
@Composable
private fun CameraScreenPreview() {
    CameraScreen(
        exerciseTitle = "Push Up",
        onBackPressed = {}
    )
}


