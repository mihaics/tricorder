package com.sysop.tricorder.feature.detail.compass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompassRose(
    heading: Float,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    Canvas(modifier = modifier.size(300.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.85f

        // Outer circle
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(2f),
        )

        // Inner circle
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = radius * 0.6f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(1f),
        )

        // Degree markings
        for (deg in 0 until 360 step 10) {
            rotate(-heading + deg.toFloat(), pivot = center) {
                val tickLength = if (deg % 30 == 0) 20f else 10f
                val tickColor = if (deg % 90 == 0) Color.White else Color.White.copy(alpha = 0.5f)
                drawLine(
                    color = tickColor,
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y - radius + tickLength),
                    strokeWidth = if (deg % 30 == 0) 2f else 1f,
                )
            }
        }

        // Direction labels
        directions.forEachIndexed { index, label ->
            val angle = index * 45f
            rotate(-heading + angle, pivot = center) {
                val labelRadius = radius * 0.72f
                val textResult = textMeasurer.measure(
                    label,
                    style = TextStyle(
                        color = if (label == "N") Color.Red else Color.White,
                        fontSize = if (label.length == 1) 18.sp else 12.sp,
                    ),
                )
                drawText(
                    textResult,
                    topLeft = Offset(
                        center.x - textResult.size.width / 2,
                        center.y - labelRadius - textResult.size.height / 2,
                    ),
                )
            }
        }

        // North needle
        rotate(-heading, pivot = center) {
            // Red north pointer
            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(center.x, center.y - radius * 0.5f),
                strokeWidth = 4f,
            )
            // White south pointer
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(center.x, center.y + radius * 0.3f),
                strokeWidth = 3f,
            )
        }

        // Center dot
        drawCircle(color = Color.White, radius = 6f, center = center)
    }
}
