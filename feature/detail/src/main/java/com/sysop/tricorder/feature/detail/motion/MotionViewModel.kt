package com.sysop.tricorder.feature.detail.motion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MotionData(
    val accelX: Float = 0f, val accelY: Float = 0f, val accelZ: Float = 0f,
    val gyroX: Float = 0f, val gyroY: Float = 0f, val gyroZ: Float = 0f,
    val magX: Float = 0f, val magY: Float = 0f, val magZ: Float = 0f,
    val rotX: Float = 0f, val rotY: Float = 0f, val rotZ: Float = 0f,
    val steps: Float = 0f,
)

@HiltViewModel
class MotionViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _motionData = MutableStateFlow(MotionData())
    val motionData: StateFlow<MotionData> = _motionData.asStateFlow()

    init {
        val provider = sensorRegistry.getProvider("motion")
        if (provider != null) {
            viewModelScope.launch {
                provider.readings().collect { reading ->
                    _motionData.update { current ->
                        current.copy(
                            accelX = reading.values["accel_x"]?.toFloat() ?: current.accelX,
                            accelY = reading.values["accel_y"]?.toFloat() ?: current.accelY,
                            accelZ = reading.values["accel_z"]?.toFloat() ?: current.accelZ,
                            gyroX = reading.values["gyro_x"]?.toFloat() ?: current.gyroX,
                            gyroY = reading.values["gyro_y"]?.toFloat() ?: current.gyroY,
                            gyroZ = reading.values["gyro_z"]?.toFloat() ?: current.gyroZ,
                            magX = reading.values["mag_x"]?.toFloat() ?: current.magX,
                            magY = reading.values["mag_y"]?.toFloat() ?: current.magY,
                            magZ = reading.values["mag_z"]?.toFloat() ?: current.magZ,
                            rotX = reading.values["rot_x"]?.toFloat() ?: current.rotX,
                            rotY = reading.values["rot_y"]?.toFloat() ?: current.rotY,
                            rotZ = reading.values["rot_z"]?.toFloat() ?: current.rotZ,
                            steps = reading.values["steps"]?.toFloat() ?: current.steps,
                        )
                    }
                }
            }
        }
    }
}
