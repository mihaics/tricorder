package com.sysop.tricorder.sensor.location

import com.google.common.truth.Truth.assertThat
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorAvailability
import com.sysop.tricorder.core.model.OverlayType
import org.junit.Test

class LocationProviderTest {

    @Test
    fun `provider has correct metadata`() {
        val provider = LocationProvider(fusedLocationClient = null, locationManager = null)
        assertThat(provider.id).isEqualTo("location")
        assertThat(provider.name).isEqualTo("Location & GNSS")
        assertThat(provider.category).isEqualTo(SensorCategory.LOCATION)
    }

    @Test
    fun `unavailable when no location services`() {
        val provider = LocationProvider(fusedLocationClient = null, locationManager = null)
        assertThat(provider.availability()).isEqualTo(SensorAvailability.UNAVAILABLE)
    }

    @Test
    fun `map overlay is markers type`() {
        val provider = LocationProvider(fusedLocationClient = null, locationManager = null)
        val overlay = provider.mapOverlay()
        assertThat(overlay).isNotNull()
        assertThat(overlay!!.type).isEqualTo(OverlayType.MARKERS)
    }
}
