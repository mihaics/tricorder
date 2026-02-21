package com.sysop.tricorder.feature.session.export

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class SessionExporter(
    private val moshi: Moshi,
) {

    private val doubleMapAdapter = moshi.adapter<Map<String, Double>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType),
    )

    private val stringMapAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java),
    )

    /**
     * Exports session and readings to CSV format.
     * Columns: timestamp, providerId, latitude, longitude, then flattened value keys.
     */
    fun exportToCsv(session: SessionEntity, readings: List<ReadingEntity>): String {
        if (readings.isEmpty()) {
            return "timestamp,providerId,latitude,longitude\n"
        }

        // Collect all unique value keys across all readings
        val allKeys = mutableSetOf<String>()
        val parsedValues = readings.map { reading ->
            val values = parseDoubleMap(reading.values)
            allKeys.addAll(values.keys)
            values
        }
        val sortedKeys = allKeys.sorted()

        val sb = StringBuilder()

        // Header
        sb.append("timestamp,providerId,latitude,longitude")
        sortedKeys.forEach { key -> sb.append(",").append(escapeCsv(key)) }
        sb.appendLine()

        // Data rows
        readings.forEachIndexed { index, reading ->
            val isoTimestamp = Instant.ofEpochMilli(reading.timestamp)
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            sb.append(isoTimestamp)
            sb.append(",").append(escapeCsv(reading.providerId))
            sb.append(",").append(reading.latitude ?: "")
            sb.append(",").append(reading.longitude ?: "")

            val values = parsedValues[index]
            sortedKeys.forEach { key ->
                sb.append(",").append(values[key] ?: "")
            }
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * Exports session and readings to JSON format.
     */
    fun exportToJson(session: SessionEntity, readings: List<ReadingEntity>): String {
        val readingsJson = readings.map { reading ->
            buildMap {
                put("timestamp", reading.timestamp.toString())
                put("providerId", reading.providerId)
                put("values", reading.values)
                put("labels", reading.labels)
                reading.latitude?.let { put("latitude", it.toString()) }
                reading.longitude?.let { put("longitude", it.toString()) }
            }
        }

        val sessionMap = buildMap {
            put("id", session.id)
            put("name", session.name)
            put("startTime", session.startTime.toString())
            session.endTime?.let { put("endTime", it.toString()) }
            put("latitude", session.latitude.toString())
            put("longitude", session.longitude.toString())
            put("activeProviders", session.activeProviders)
        }

        val export = mapOf(
            "session" to sessionMap,
            "readings" to readingsJson,
        )

        val adapter = moshi.adapter<Any>(Any::class.java).indent("  ")
        return adapter.toJson(export)
    }

    /**
     * Exports session and readings as a GPX track with waypoints.
     */
    fun exportToGpx(session: SessionEntity, readings: List<ReadingEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="Tricorder" xmlns="http://www.topografix.com/GPX/1/1">""")

        // Metadata
        sb.appendLine("  <metadata>")
        sb.appendLine("    <name>${escapeXml(session.name)}</name>")
        sb.appendLine("    <time>${formatGpxTime(session.startTime)}</time>")
        sb.appendLine("  </metadata>")

        // Waypoints for readings that have location data
        val geoReadings = readings.filter { it.latitude != null && it.longitude != null }

        geoReadings.forEach { reading ->
            sb.appendLine("""  <wpt lat="${reading.latitude}" lon="${reading.longitude}">""")
            sb.appendLine("    <time>${formatGpxTime(reading.timestamp)}</time>")
            sb.appendLine("    <name>${escapeXml(reading.providerId)}</name>")
            sb.appendLine("    <desc>${escapeXml(reading.values)}</desc>")
            sb.appendLine("  </wpt>")
        }

        // Track
        if (geoReadings.isNotEmpty()) {
            sb.appendLine("  <trk>")
            sb.appendLine("    <name>${escapeXml(session.name)}</name>")
            sb.appendLine("    <trkseg>")
            geoReadings.forEach { reading ->
                sb.appendLine("""      <trkpt lat="${reading.latitude}" lon="${reading.longitude}">""")
                sb.appendLine("        <time>${formatGpxTime(reading.timestamp)}</time>")
                sb.appendLine("      </trkpt>")
            }
            sb.appendLine("    </trkseg>")
            sb.appendLine("  </trk>")
        }

        sb.appendLine("</gpx>")
        return sb.toString()
    }

    private fun parseDoubleMap(json: String): Map<String, Double> {
        return try {
            doubleMapAdapter.fromJson(json) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun formatGpxTime(epochMilli: Long): String {
        return Instant.ofEpochMilli(epochMilli)
            .atOffset(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
