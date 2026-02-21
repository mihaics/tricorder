package com.sysop.tricorder.feature.detail.gnss

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Constellation colors
private val GPS_COLOR = Color(0xFF2196F3) // Blue
private val GLONASS_COLOR = Color(0xFFF44336) // Red
private val GALILEO_COLOR = Color(0xFFFF9800) // Orange
private val BEIDOU_COLOR = Color(0xFF4CAF50) // Green
private val OTHER_COLOR = Color(0xFF9E9E9E) // Grey

@Composable
fun SkyPlotCanvas(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(300.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2 * 0.9f

        // Concentric circles (0, 30, 60, 90 degrees elevation)
        for (i in 1..3) {
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = maxRadius * i / 3,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(1f),
            )
        }
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = maxRadius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(2f),
        )

        // Cross lines (N-S, E-W)
        drawLine(Color.White.copy(alpha = 0.2f), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius))
        drawLine(Color.White.copy(alpha = 0.2f), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y))

        // Plot satellites
        for (sat in satellites) {
            val elevationRatio = (90f - sat.elevation) / 90f // 0 = center, 1 = edge
            val r = maxRadius * elevationRatio
            val angleRad = Math.toRadians(sat.azimuth.toDouble() - 90) // 0=North=up
            val x = center.x + (r * cos(angleRad)).toFloat()
            val y = center.y + (r * sin(angleRad)).toFloat()

            val color = when (sat.constellation) {
                1 -> GPS_COLOR
                3 -> GLONASS_COLOR
                6 -> GALILEO_COLOR
                5 -> BEIDOU_COLOR
                else -> OTHER_COLOR
            }

            val dotRadius = if (sat.usedInFix) 8f else 5f
            drawCircle(color = color, radius = dotRadius, center = Offset(x, y))
            if (!sat.usedInFix) {
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(x, y),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(2f),
                )
            }
        }
    }
}
