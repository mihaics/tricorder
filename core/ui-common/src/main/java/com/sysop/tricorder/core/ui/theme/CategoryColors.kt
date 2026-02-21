package com.sysop.tricorder.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.sysop.tricorder.core.model.SensorCategory

fun SensorCategory.color(): Color = when (this) {
    SensorCategory.MOTION -> MotionCyan
    SensorCategory.ENVIRONMENT -> EnvironmentGreen
    SensorCategory.LOCATION -> LocationBlue
    SensorCategory.RF -> RfOrange
    SensorCategory.AUDIO -> AudioPurple
    SensorCategory.CAMERA -> CameraPink
    SensorCategory.WEATHER -> WeatherYellow
    SensorCategory.AIR_QUALITY -> AirQualityTeal
    SensorCategory.AVIATION -> AviationSkyBlue
    SensorCategory.SEISMIC -> SeismicRed
    SensorCategory.RADIATION -> RadiationAmber
    SensorCategory.SPACE -> SpaceIndigo
    SensorCategory.TIDES -> TidesDeepBlue
}
