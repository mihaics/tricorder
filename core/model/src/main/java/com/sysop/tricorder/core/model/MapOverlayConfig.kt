package com.sysop.tricorder.core.model

data class MapOverlayConfig(
    val type: OverlayType,
    val colorScheme: List<Long> = emptyList(),
    val opacity: Float = 0.7f,
)

enum class OverlayType {
    HEATMAP,
    MARKERS,
    CIRCLES,
    POLYLINE,
    VECTOR_FIELD,
    SKY_PLOT,
}
