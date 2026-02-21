package com.sysop.tricorder.core.sensorapi

import com.sysop.tricorder.core.model.SensorCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRegistry @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards SensorProvider>,
) {
    fun getProviders(): Set<SensorProvider> = providers

    fun getProvidersByCategory(category: SensorCategory): List<SensorProvider> =
        providers.filter { it.category == category }

    fun getProvider(id: String): SensorProvider? =
        providers.find { it.id == id }
}
