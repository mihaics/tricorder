package com.sysop.tricorder.sensor.rf

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject

class BleScanProvider @Inject constructor(
    private val bluetoothManager: BluetoothManager?,
) : SensorProvider {

    override val id = "ble-scan"
    override val name = "Bluetooth Scanner"
    override val category = SensorCategory.RF

    override fun availability(): SensorAvailability {
        val adapter = bluetoothManager?.adapter
        return if (adapter != null && adapter.isEnabled) SensorAvailability.REQUIRES_PERMISSION
        else SensorAvailability.UNAVAILABLE
    }

    @Suppress("MissingPermission")
    override fun readings(): Flow<SensorReading> = callbackFlow {
        val scanner = bluetoothManager?.adapter?.bluetoothLeScanner
            ?: run { close(); return@callbackFlow }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(SensorReading(
                    providerId = id,
                    category = category,
                    timestamp = Instant.now(),
                    values = mapOf(
                        "rssi" to result.rssi.toDouble(),
                        "tx_power" to result.txPower.toDouble(),
                    ),
                    labels = mapOf(
                        "device_name" to (result.device.name ?: "Unknown"),
                        "mac_address" to result.device.address,
                    ),
                ))
            }
        }

        try {
            scanner.startScan(callback)
        } catch (_: SecurityException) {
            close()
            return@callbackFlow
        }
        awaitClose {
            try { scanner.stopScan(callback) } catch (_: SecurityException) {}
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.MARKERS)
}
