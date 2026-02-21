package com.sysop.tricorder.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.datastore.TricorderPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: TricorderPreferences,
) : ViewModel() {

    val openWeatherMapKey: StateFlow<String> = preferences.openWeatherMapKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val waqiKey: StateFlow<String> = preferences.waqiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val n2yoKey: StateFlow<String> = preferences.n2yoKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val openCellIdKey: StateFlow<String> = preferences.openCellIdKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val defaultSampleRateMs: StateFlow<Long> = preferences.defaultSampleRateMs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1000L)

    val sessionRetentionDays: StateFlow<Int> = preferences.sessionRetentionDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 30)

    fun setOpenWeatherMapKey(key: String) {
        viewModelScope.launch { preferences.setOpenWeatherMapKey(key) }
    }

    fun setWaqiKey(key: String) {
        viewModelScope.launch { preferences.setWaqiKey(key) }
    }

    fun setN2yoKey(key: String) {
        viewModelScope.launch { preferences.setN2yoKey(key) }
    }

    fun setOpenCellIdKey(key: String) {
        viewModelScope.launch { preferences.setOpenCellIdKey(key) }
    }

    fun setDefaultSampleRateMs(ms: Long) {
        viewModelScope.launch { preferences.setDefaultSampleRateMs(ms) }
    }

    fun setSessionRetentionDays(days: Int) {
        viewModelScope.launch { preferences.setSessionRetentionDays(days) }
    }
}
