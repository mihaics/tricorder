package com.sysop.tricorder.feature.detail.camera

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sysop.tricorder.core.ui.theme.CameraPink
import com.sysop.tricorder.core.ui.theme.DarkSurface
import com.sysop.tricorder.core.ui.theme.DarkSurfaceVariant
import com.sysop.tricorder.sensor.camera.ColorAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraAnalysisScreen(
    onBack: () -> Unit = {},
    viewModel: CameraAnalysisViewModel = hiltViewModel(),
) {
    val brightness by viewModel.brightness.collectAsState()
    val avgR by viewModel.avgR.collectAsState()
    val avgG by viewModel.avgG.collectAsState()
    val avgB by viewModel.avgB.collectAsState()
    val useFrontCamera by viewModel.useFrontCamera.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera Analysis", color = CameraPink) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleCamera() }) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            // Camera preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                val cameraSelector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                    else CameraSelector.DEFAULT_BACK_CAMERA

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(analysisExecutor, ColorAnalyzer { r, g, b, br ->
                                        viewModel.updateColorAnalysis(r, g, b, br)
                                    })
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis,
                                )
                            } catch (_: Exception) {
                                // Camera not available
                            }
                        }, ContextCompat.getMainExecutor(context))
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                // Crosshair overlay
                Box(modifier = Modifier.size(2.dp, 60.dp).background(Color.White.copy(alpha = 0.4f)))
                Box(modifier = Modifier.size(60.dp, 2.dp).background(Color.White.copy(alpha = 0.4f)))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color analysis card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Color Analysis (Center Region)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        ColorChannel("R", avgR, Color.Red)
                        ColorChannel("G", avgG, Color.Green)
                        ColorChannel("B", avgB, Color(0xFF4488FF))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Brightness bar
                    Text(
                        "Brightness",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { (brightness / 100f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CameraPink,
                            trackColor = DarkSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "%.1f%%".format(brightness),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color swatch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Average Color",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp, 24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Color(
                                        red = (avgR / 255f).toFloat().coerceIn(0f, 1f),
                                        green = (avgG / 255f).toFloat().coerceIn(0f, 1f),
                                        blue = (avgB / 255f).toFloat().coerceIn(0f, 1f),
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorChannel(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "%.0f".format(value),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
    }
}
