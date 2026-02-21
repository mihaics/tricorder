package com.sysop.tricorder.core.model

enum class SensorCategory(val displayName: String, val icon: String) {
    MOTION("Motion", "compass"),
    ENVIRONMENT("Environment", "thermometer"),
    LOCATION("Location", "satellite"),
    RF("RF & Connectivity", "radio"),
    AUDIO("Audio", "mic"),
    CAMERA("Camera", "camera"),
    WEATHER("Weather", "cloud"),
    AIR_QUALITY("Air Quality", "wind"),
    AVIATION("Aviation", "plane"),
    SEISMIC("Seismic", "activity"),
    RADIATION("Radiation", "alert-triangle"),
    SPACE("Space", "star"),
    TIDES("Tides", "waves"),
}
