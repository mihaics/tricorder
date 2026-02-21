package com.sysop.tricorder.sensor.camera

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ColorAnalyzer(
    private val onResult: (avgR: Double, avgG: Double, avgB: Double, brightness: Double) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        if (image.format != ImageFormat.YUV_420_888) {
            image.close()
            return
        }

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val width = image.width
        val height = image.height
        val yRowStride = image.planes[0].rowStride
        val uvRowStride = image.planes[1].rowStride
        val uvPixelStride = image.planes[1].pixelStride

        // Sample center region (middle 1/4), every 8th pixel for speed
        val startX = width / 4
        val endX = width * 3 / 4
        val startY = height / 4
        val endY = height * 3 / 4

        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var totalY = 0L
        var count = 0

        for (y in startY until endY step 8) {
            for (x in startX until endX step 8) {
                val yVal = yBuffer.get(y * yRowStride + x).toInt() and 0xFF
                val uvX = x / 2
                val uvY = y / 2
                val uVal = (uBuffer.get(uvY * uvRowStride + uvX * uvPixelStride).toInt() and 0xFF) - 128
                val vVal = (vBuffer.get(uvY * uvRowStride + uvX * uvPixelStride).toInt() and 0xFF) - 128

                // YUV to RGB (BT.601)
                val r = (yVal + 1.402 * vVal).toInt().coerceIn(0, 255)
                val g = (yVal - 0.344136 * uVal - 0.714136 * vVal).toInt().coerceIn(0, 255)
                val b = (yVal + 1.772 * uVal).toInt().coerceIn(0, 255)

                totalR += r
                totalG += g
                totalB += b
                totalY += yVal
                count++
            }
        }

        if (count > 0) {
            val avgBrightness = totalY.toDouble() / count / 255.0 * 100.0
            onResult(
                totalR.toDouble() / count,
                totalG.toDouble() / count,
                totalB.toDouble() / count,
                avgBrightness,
            )
        }

        image.close()
    }
}
