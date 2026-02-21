package com.sysop.tricorder.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tricorder_prefs")

@Singleton
class TricorderPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val OPENWEATHERMAP_KEY = stringPreferencesKey("openweathermap_api_key")
        val SESSION_RETENTION_DAYS = intPreferencesKey("session_retention_days")
        val DEFAULT_SAMPLE_RATE_MS = longPreferencesKey("default_sample_rate_ms")
    }

    val openWeatherMapKey: Flow<String> = context.dataStore.data.map { it[Keys.OPENWEATHERMAP_KEY] ?: "" }

    val sessionRetentionDays: Flow<Int> = context.dataStore.data.map { it[Keys.SESSION_RETENTION_DAYS] ?: 30 }

    val defaultSampleRateMs: Flow<Long> = context.dataStore.data.map { it[Keys.DEFAULT_SAMPLE_RATE_MS] ?: 1000L }

    suspend fun setOpenWeatherMapKey(key: String) {
        context.dataStore.edit { it[Keys.OPENWEATHERMAP_KEY] = key }
    }

    suspend fun setSessionRetentionDays(days: Int) {
        context.dataStore.edit { it[Keys.SESSION_RETENTION_DAYS] = days }
    }

    suspend fun setDefaultSampleRateMs(ms: Long) {
        context.dataStore.edit { it[Keys.DEFAULT_SAMPLE_RATE_MS] = ms }
    }
}
