package com.sysop.tricorder.feature.detail.compass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _fieldStrength = MutableStateFlow(0f)
    val fieldStrength: StateFlow<Float> = _fieldStrength.asStateFlow()

    init {
        val motionProvider = sensorRegistry.getProvider("motion")
        if (motionProvider != null) {
            viewModelScope.launch {
                motionProvider.readings().collect { reading ->
                    val magX = reading.values["mag_x"]?.toFloat() ?: 0f
                    val magY = reading.values["mag_y"]?.toFloat() ?: 0f
                    val magZ = reading.values["mag_z"]?.toFloat() ?: 0f

                    // Compute heading from magnetometer (simplified, not tilt-compensated)
                    val headingRad = kotlin.math.atan2(magY.toDouble(), magX.toDouble())
                    var headingDeg = Math.toDegrees(headingRad).toFloat()
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
