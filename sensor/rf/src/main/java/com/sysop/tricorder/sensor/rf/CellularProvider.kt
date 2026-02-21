package com.sysop.tricorder.sensor.rf

import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import java.util.concurrent.Executors
import javax.inject.Inject

class CellularProvider @Inject constructor(
    private val telephonyManager: TelephonyManager?,
) : SensorProvider {

    override val id = "cellular"
    override val name = "Cellular"
    override val category = SensorCategory.RF

    override fun availability(): SensorAvailability {
        return if (telephonyManager != null) SensorAvailability.REQUIRES_PERMISSION
        else SensorAvailability.UNAVAILABLE
    }

    @Suppress("MissingPermission")
    override fun readings(): Flow<SensorReading> = callbackFlow {
        val tm = telephonyManager ?: run { close(); return@callbackFlow }
        val executor = Executors.newSingleThreadExecutor()

        val callback = object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
                for (cell in cellInfo) {
                    val values = mutableMapOf<String, Double>()
                    val labels = mutableMapOf<String, String>()

                    when (cell) {
                        is CellInfoLte -> {
                            values["rssi"] = cell.cellSignalStrength.rssi.toDouble()
                            values["rsrp"] = cell.cellSignalStrength.rsrp.toDouble()
                            values["rsrq"] = cell.cellSignalStrength.rsrq.toDouble()
                            values["signal_level"] = cell.cellSignalStrength.level.toDouble()
                            values["cell_id"] = cell.cellIdentity.ci.toDouble()
                            values["mcc"] = (cell.cellIdentity.mccString?.toDoubleOrNull() ?: 0.0)
                            values["mnc"] = (cell.cellIdentity.mncString?.toDoubleOrNull() ?: 0.0)
                            labels["network_type"] = "LTE"
                        }
                        is CellInfoNr -> {
                            values["signal_level"] = cell.cellSignalStrength.level.toDouble()
                            labels["network_type"] = "5G NR"
                        }
                        is CellInfoWcdma -> {
                            values["signal_level"] = cell.cellSignalStrength.level.toDouble()
                            labels["network_type"] = "WCDMA"
                        }
                        is CellInfoGsm -> {
                            values["signal_level"] = cell.cellSignalStrength.level.toDouble()
                            labels["network_type"] = "GSM"
                        }
                    }

                    trySend(SensorReading(
                        providerId = id,
                        category = category,
                        timestamp = Instant.now(),
                        values = values,
                        labels = labels,
                    ))
                }
            }
        }

        try {
            tm.registerTelephonyCallback(executor, callback)
        } catch (_: SecurityException) {
            executor.shutdown()
            close()
            return@callbackFlow
        }
        awaitClose {
            tm.unregisterTelephonyCallback(callback)
            executor.shutdown()
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.CIRCLES)
}
