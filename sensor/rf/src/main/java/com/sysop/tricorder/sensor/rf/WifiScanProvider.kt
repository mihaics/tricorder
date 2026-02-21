package com.sysop.tricorder.sensor.rf

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject

class WifiScanProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager?,
) : SensorProvider {

    override val id = "wifi-scan"
    override val name = "WiFi Scanner"
    override val category = SensorCategory.RF

    override fun availability(): SensorAvailability {
        return if (wifiManager != null) SensorAvailability.REQUIRES_PERMISSION
        else SensorAvailability.UNAVAILABLE
    }

    @Suppress("MissingPermission")
    override fun readings(): Flow<SensorReading> = callbackFlow {
        val wm = wifiManager ?: run { close(); return@callbackFlow }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val results = wm.scanResults
                for (result in results) {
                    trySend(SensorReading(
                        providerId = id,
                        category = category,
                        timestamp = Instant.now(),
                        values = mapOf(
                            "rssi" to result.level.toDouble(),
                            "frequency" to result.frequency.toDouble(),
                            "channel_width" to result.channelWidth.toDouble(),
                        ),
                        labels = mapOf(
                            "ssid" to result.SSID,
                            "bssid" to result.BSSID,
                        ),
                    ))
                }
            }
        }

        try {
            context.registerReceiver(
                receiver,
                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            )
            wm.startScan()
        } catch (_: SecurityException) {
            close()
            return@callbackFlow
        }

        awaitClose { context.unregisterReceiver(receiver) }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.CIRCLES)
}
