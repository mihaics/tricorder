package com.sysop.tricorder.permission

import android.Manifest
import com.sysop.tricorder.core.model.SensorCategory

/**
 * Groups Android permissions by [SensorCategory] and provides human-readable
 * explanations for why each permission group is needed.
 */
object PermissionManager {

    private val permissionMap: Map<SensorCategory, List<String>> = mapOf(
        SensorCategory.LOCATION to listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
        SensorCategory.RF to listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.NEARBY_WIFI_DEVICES,
        ),
        SensorCategory.CAMERA to listOf(
            Manifest.permission.CAMERA,
        ),
        SensorCategory.AUDIO to listOf(
            Manifest.permission.RECORD_AUDIO,
        ),
        SensorCategory.MOTION to listOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BODY_SENSORS,
        ),
    )

    private val explanationMap: Map<SensorCategory, String> = mapOf(
        SensorCategory.LOCATION to "Location & GPS - required for satellite positioning, mapping, and location-based sensor readings",
        SensorCategory.RF to "Bluetooth & WiFi scanning - required to detect nearby wireless networks and Bluetooth devices",
        SensorCategory.CAMERA to "Camera analysis - required for luminance measurement and visual spectrum analysis",
        SensorCategory.AUDIO to "Audio spectrum analysis - required to capture audio for frequency and decibel analysis",
        SensorCategory.MOTION to "Motion & step tracking - required for pedometer, activity detection, and body sensor access",
    )

    /** Returns the list of Android permissions needed for the given sensor category. */
    fun getPermissionsForCategory(category: SensorCategory): List<String> {
        return permissionMap[category] ?: emptyList()
    }

    /** Returns a human-readable explanation of why permissions are needed for the category. */
    fun getExplanation(category: SensorCategory): String {
        return explanationMap[category] ?: "${category.displayName} - no special permissions required"
    }

    /** Returns all permissions across every sensor category. */
    fun allPermissions(): List<String> {
        return permissionMap.values.flatten().distinct()
    }

    /** Returns only the sensor categories that require runtime permissions. */
    fun categoriesRequiringPermissions(): List<SensorCategory> {
        return permissionMap.keys.toList()
    }
}
