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

        val columnWidth = size.width / 100f
        val maxBins = spectrogramData.maxOf { it.size }.coerceAtLeast(1)
        val binHeight = size.height / maxBins

        spectrogramData.forEachIndexed { columnIndex, magnitudes ->
            val x = columnIndex * columnWidth
            magnitudes.forEachIndexed { binIndex, magnitude ->
                val normalized = (magnitude / 100f).coerceIn(0f, 1f)
                val color = spectrogramColor(normalized)
                drawRect(
                    color = color,
                    topLeft = Offset(x, size.height - (binIndex + 1) * binHeight),
                    size = Size(columnWidth, binHeight),
                )
            }
        }
    }
}

private fun spectrogramColor(value: Float): Color {
    return when {
        value < 0.25f -> Color(0f, 0f, value * 4f) // dark blue to blue
        value < 0.5f -> Color(0f, (value - 0.25f) * 4f, 1f) // blue to cyan
        value < 0.75f -> Color((value - 0.5f) * 4f, 1f, 1f - (value - 0.5f) * 4f) // cyan to yellow
        else -> Color(1f, 1f - (value - 0.75f) * 4f, 0f) // yellow to red
    }
}
