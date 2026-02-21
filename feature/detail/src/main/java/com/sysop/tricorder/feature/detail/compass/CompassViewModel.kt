package com.sysop.tricorder.feature.detail.compass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)

@HiltViewModel
class CompassViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _fieldStrength = MutableStateFlow(0f)
    val fieldStrength: StateFlow<Float> = _fieldStrength.asStateFlow()

    // Low-pass filter coefficient (0 = no smoothing, 1 = frozen)
    private val alpha = 0.85f
    private var smoothedHeadingX = 0.0
    private var smoothedHeadingY = 0.0

    init {
        val motionProvider = sensorRegistry.getProvider("motion")
        if (motionProvider != null) {
            viewModelScope.launch {
                motionProvider.readings()
                    .sample(50) // ~20fps max update rate
                    .collect { reading ->
                        val magX = reading.values["mag_x"]?.toFloat() ?: 0f
                        val magY = reading.values["mag_y"]?.toFloat() ?: 0f
                        val magZ = reading.values["mag_z"]?.toFloat() ?: 0f

                        // Compute heading from magnetometer
                        val headingRad = kotlin.math.atan2(magY.toDouble(), magX.toDouble())

                        // Low-pass filter using circular averaging to avoid wrap-around jitter
                        smoothedHeadingX = alpha * smoothedHeadingX + (1 - alpha) * kotlin.math.cos(headingRad)
                        smoothedHeadingY = alpha * smoothedHeadingY + (1 - alpha) * kotlin.math.sin(headingRad)
                        var headingDeg = Math.toDegrees(kotlin.math.atan2(smoothedHeadingY, smoothedHeadingX)).toFloat()
                        if (headingDeg < 0) headingDeg += 360f
                        _heading.value = headingDeg

                        // Field strength in microTesla
                        _fieldStrength.value = kotlin.math.sqrt(
                            (magX * magX + magY * magY + magZ * magZ).toDouble()
                        ).toFloat()
                    }
            }
        }
    }
}
