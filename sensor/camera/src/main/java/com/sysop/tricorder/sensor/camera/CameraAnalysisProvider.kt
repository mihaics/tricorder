package com.sysop.tricorder.sensor.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject

class CameraAnalysisProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : SensorProvider {

    override val id = "camera"
    override val name = "Camera Analysis"
    override val category = SensorCategory.CAMERA

    override fun availability(): SensorAvailability {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        return if (hasPermission) SensorAvailability.AVAILABLE
        else SensorAvailability.REQUIRES_PERMISSION
    }

    override fun readings(): Flow<SensorReading> = callbackFlow {
        // Camera analysis is triggered from the detail view via CameraX.
        // This provider emits readings when the ColorAnalyzer callback fires.
        // The actual CameraX binding happens in the UI layer.
        // Here we provide a callback-driven flow that the UI can push into.

        awaitClose {}
    }

    val analyzer = ColorAnalyzer { avgR, avgG, avgB, brightness ->
        // This will be connected when the camera detail view is active
    }

    override fun mapOverlay(): MapOverlayConfig? = null // Camera doesn't have a map overlay
}
