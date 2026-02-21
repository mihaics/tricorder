package com.sysop.tricorder.sensor.environment

import com.google.common.truth.Truth.assertThat
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorAvailability
import com.sysop.tricorder.core.model.OverlayType
import org.junit.Test

class EnvironmentSensorProviderTest {

    @Test
    fun `provider has correct metadata`() {
        val provider = EnvironmentSensorProvider(sensorManager = null)
        assertThat(provider.id).isEqualTo("environment")
        assertThat(provider.name).isEqualTo("Environment")
        assertThat(provider.category).isEqualTo(SensorCategory.ENVIRONMENT)
    }

    @Test
    fun `unavailable when no sensor manager`() {
        val provider = EnvironmentSensorProvider(sensorManager = null)
        assertThat(provider.availability()).isEqualTo(SensorAvailability.UNAVAILABLE)
    }

    @Test
    fun `map overlay is heatmap type`() {
        val provider = EnvironmentSensorProvider(sensorManager = null)
        val overlay = provider.mapOverlay()
        assertThat(overlay).isNotNull()
        assertThat(overlay!!.type).isEqualTo(OverlayType.HEATMAP)
    }
}
