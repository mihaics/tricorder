package com.sysop.tricorder.sensor.motion

import com.google.common.truth.Truth.assertThat
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorAvailability
import com.sysop.tricorder.core.model.OverlayType
import org.junit.Test

class MotionSensorProviderTest {

    @Test
    fun `provider has correct metadata`() {
        val provider = MotionSensorProvider(sensorManager = null)
        assertThat(provider.id).isEqualTo("motion")
        assertThat(provider.name).isEqualTo("Motion & Orientation")
        assertThat(provider.category).isEqualTo(SensorCategory.MOTION)
    }

    @Test
    fun `unavailable when no sensor manager`() {
        val provider = MotionSensorProvider(sensorManager = null)
        assertThat(provider.availability()).isEqualTo(SensorAvailability.UNAVAILABLE)
    }

    @Test
    fun `map overlay is vector field type`() {
        val provider = MotionSensorProvider(sensorManager = null)
        val overlay = provider.mapOverlay()
        assertThat(overlay).isNotNull()
        assertThat(overlay!!.type).isEqualTo(OverlayType.VECTOR_FIELD)
    }
}
