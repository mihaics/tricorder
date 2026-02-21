package com.sysop.tricorder.core.sensorapi

import com.google.common.truth.Truth.assertThat
import com.sysop.tricorder.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Test

class SensorRegistryTest {

    private val fakeProvider = object : SensorProvider {
        override val id = "test-sensor"
        override val name = "Test Sensor"
        override val category = SensorCategory.ENVIRONMENT
        override fun availability() = SensorAvailability.AVAILABLE
        override fun readings(): Flow<SensorReading> = emptyFlow()
        override fun mapOverlay(): MapOverlayConfig? = null
    }

    @Test
    fun `getProviders returns all registered providers`() {
        val registry = SensorRegistry(setOf(fakeProvider))
        assertThat(registry.getProviders()).containsExactly(fakeProvider)
    }

    @Test
    fun `getProvidersByCategory filters correctly`() {
        val registry = SensorRegistry(setOf(fakeProvider))
        assertThat(registry.getProvidersByCategory(SensorCategory.ENVIRONMENT))
            .containsExactly(fakeProvider)
        assertThat(registry.getProvidersByCategory(SensorCategory.MOTION))
            .isEmpty()
    }

    @Test
    fun `getProvider returns by id`() {
        val registry = SensorRegistry(setOf(fakeProvider))
        assertThat(registry.getProvider("test-sensor")).isEqualTo(fakeProvider)
        assertThat(registry.getProvider("nonexistent")).isNull()
    }
}
