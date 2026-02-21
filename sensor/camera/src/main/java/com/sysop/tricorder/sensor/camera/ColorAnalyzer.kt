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
        val width = image.width
        val height = image.height

        // Sample center region (middle 1/4 of the image)
        val startX = width / 4
        val endX = width * 3 / 4
        val startY = height / 4
        val endY = height * 3 / 4
        val rowStride = image.planes[0].rowStride

        var totalY = 0L
        var count = 0

        for (y in startY until endY step 4) {
            for (x in startX until endX step 4) {
                val yVal = yBuffer.get(y * rowStride + x).toInt() and 0xFF
                totalY += yVal
                count++
            }
        }

        val avgBrightness = if (count > 0) totalY.toDouble() / count else 0.0

        // Approximate RGB from Y (luminance only, since full YUV->RGB is expensive)
        // For the detail view, full conversion will be done at lower frequency
        val normalizedBrightness = avgBrightness / 255.0

        onResult(
            avgBrightness, // approximate, detail view does full conversion
            avgBrightness,
            avgBrightness,
            normalizedBrightness * 100.0,
        )

        image.close()
    }
}
