package com.sysop.tricorder.feature.detail.camera

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CameraAnalysisViewModel @Inject constructor() : ViewModel() {

    private val _brightness = MutableStateFlow(0.0)
    val brightness: StateFlow<Double> = _brightness.asStateFlow()

    private val _avgR = MutableStateFlow(0.0)
    val avgR: StateFlow<Double> = _avgR.asStateFlow()

    private val _avgG = MutableStateFlow(0.0)
    val avgG: StateFlow<Double> = _avgG.asStateFlow()

    private val _avgB = MutableStateFlow(0.0)
    val avgB: StateFlow<Double> = _avgB.asStateFlow()

    private val _useFrontCamera = MutableStateFlow(false)
    val useFrontCamera: StateFlow<Boolean> = _useFrontCamera.asStateFlow()

    fun updateColorAnalysis(r: Double, g: Double, b: Double, brightness: Double) {
        _avgR.value = r
        _avgG.value = g
        _avgB.value = b
        _brightness.value = brightness
    }

    fun toggleCamera() {
        _useFrontCamera.update { !it }
    }
}
