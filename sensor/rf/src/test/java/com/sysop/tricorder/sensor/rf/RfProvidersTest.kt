package com.sysop.tricorder.sensor.rf

import com.google.common.truth.Truth.assertThat
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorAvailability
import com.sysop.tricorder.core.model.OverlayType
import org.junit.Test

class RfProvidersTest {

    @Test
    fun `ble provider has correct metadata and is unavailable without bluetooth`() {
        val provider = BleScanProvider(bluetoothManager = null)
        assertThat(provider.id).isEqualTo("ble-scan")
        assertThat(provider.name).isEqualTo("Bluetooth Scanner")
        assertThat(provider.category).isEqualTo(SensorCategory.RF)
        assertThat(provider.availability()).isEqualTo(SensorAvailability.UNAVAILABLE)
        assertThat(provider.mapOverlay()!!.type).isEqualTo(OverlayType.MARKERS)
    }

    @Test
    fun `cellular provider has correct metadata and is unavailable without telephony`() {
        val provider = CellularProvider(telephonyManager = null)
        assertThat(provider.id).isEqualTo("cellular")
        assertThat(provider.name).isEqualTo("Cellular")
        assertThat(provider.category).isEqualTo(SensorCategory.RF)
        assertThat(provider.availability()).isEqualTo(SensorAvailability.UNAVAILABLE)
        assertThat(provider.mapOverlay()!!.type).isEqualTo(OverlayType.CIRCLES)
    }
}
