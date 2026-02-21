package com.sysop.tricorder.feature.detail.audio

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun SpectrogramCanvas(
    spectrogramData: List<FloatArray>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (spectrogramData.isEmpty()) return@Canvas

        val columns = spectrogramData.size
        val columnWidth = size.width / columns
        // Only show lower half of FFT bins (useful frequency range)
        val maxBins = spectrogramData.maxOf { it.size / 2 }.coerceAtLeast(1)
        val binHeight = size.height / maxBins

        spectrogramData.forEachIndexed { columnIndex, magnitudes ->
            val x = columnIndex * columnWidth
            val binsToShow = (magnitudes.size / 2).coerceAtMost(maxBins)
            for (binIndex in 0 until binsToShow) {
                val value = magnitudes[binIndex]
                if (value > 0.01f) {
                    drawRect(
                        color = spectrogramColor(value),
                        topLeft = Offset(x, size.height - (binIndex + 1) * binHeight),
                        size = Size(columnWidth + 1f, binHeight + 1f),
                    )
                }
            }
        }
    }
}

private fun spectrogramColor(value: Float): Color {
    return when {
        value < 0.2f -> Color(0f, 0f, value * 5f)
        value < 0.4f -> Color(0f, (value - 0.2f) * 5f, 1f)
        value < 0.6f -> Color((value - 0.4f) * 5f, 1f, 1f - (value - 0.4f) * 5f)
        value < 0.8f -> Color(1f, 1f, (value - 0.6f) * 5f)
        else -> Color(1f, 1f - (value - 0.8f) * 5f, 0f)
    }
}
