package com.sysop.tricorder.feature.session.export

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity
import org.junit.Before
import org.junit.Test

class SessionExporterTest {

    private lateinit var exporter: SessionExporter
    private lateinit var session: SessionEntity
    private lateinit var readings: List<ReadingEntity>

    @Before
    fun setup() {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        exporter = SessionExporter(moshi)

        session = SessionEntity(
            id = "test-session-id",
            name = "Test Session",
            startTime = 1700000000000L, // 2023-11-14T22:13:20Z
            endTime = 1700000060000L,   // +60 seconds
            latitude = 47.3769,
            longitude = 8.5417,
            activeProviders = "motion,environment",
        )

        readings = listOf(
            ReadingEntity(
                id = 1,
                sessionId = "test-session-id",
                timestamp = 1700000001000L,
                providerId = "motion",
                values = """{"accel_x":1.5,"accel_y":2.3,"accel_z":9.8}""",
                labels = """{"unit":"m/s2"}""",
                latitude = 47.3769,
                longitude = 8.5417,
            ),
            ReadingEntity(
                id = 2,
                sessionId = "test-session-id",
                timestamp = 1700000002000L,
                providerId = "environment",
                values = """{"temp":22.5,"humidity":45.0}""",
                labels = """{"unit":"celsius"}""",
                latitude = 47.3770,
                longitude = 8.5418,
            ),
            ReadingEntity(
                id = 3,
                sessionId = "test-session-id",
                timestamp = 1700000003000L,
                providerId = "motion",
                values = """{"accel_x":1.6,"accel_y":2.4,"accel_z":9.7}""",
                labels = """{}""",
                latitude = null,
                longitude = null,
            ),
        )
    }

    // --- CSV Tests ---

    @Test
    fun `exportToCsv produces correct header`() {
        val csv = exporter.exportToCsv(session, readings)
        val lines = csv.lines()

        assertThat(lines.first()).isEqualTo(
            "timestamp,providerId,latitude,longitude,accel_x,accel_y,accel_z,humidity,temp",
        )
    }

    @Test
    fun `exportToCsv produces correct number of data rows`() {
        val csv = exporter.exportToCsv(session, readings)
        val lines = csv.trimEnd().lines()

        // 1 header + 3 data rows
        assertThat(lines).hasSize(4)
    }

    @Test
    fun `exportToCsv formats timestamps as ISO`() {
        val csv = exporter.exportToCsv(session, readings)
        val lines = csv.trimEnd().lines()
        val firstDataLine = lines[1]

        assertThat(firstDataLine).startsWith("2023-11-14T22:13:21")
    }

    @Test
    fun `exportToCsv handles missing values with empty columns`() {
        val csv = exporter.exportToCsv(session, readings)
        val lines = csv.trimEnd().lines()

        // Environment reading (index 1) has no accel values
        val envLine = lines[2]
        val columns = envLine.split(",")
        // accel_x, accel_y, accel_z should be empty for environment reading
        assertThat(columns[4]).isEmpty() // accel_x
        assertThat(columns[5]).isEmpty() // accel_y
        assertThat(columns[6]).isEmpty() // accel_z
        // humidity and temp should have values
        assertThat(columns[7]).isEqualTo("45.0")
        assertThat(columns[8]).isEqualTo("22.5")
    }

    @Test
    fun `exportToCsv handles empty readings`() {
        val csv = exporter.exportToCsv(session, emptyList())
        assertThat(csv).isEqualTo("timestamp,providerId,latitude,longitude\n")
    }

    @Test
    fun `exportToCsv handles missing location`() {
        val csv = exporter.exportToCsv(session, readings)
        val lines = csv.trimEnd().lines()
        val lastLine = lines[3]
        val columns = lastLine.split(",")

        // Third reading has null lat/lon
        assertThat(columns[2]).isEmpty()
        assertThat(columns[3]).isEmpty()
    }

    // --- JSON Tests ---

    @Test
    fun `exportToJson contains session data`() {
        val json = exporter.exportToJson(session, readings)

        assertThat(json).contains("\"id\"")
        assertThat(json).contains("test-session-id")
        assertThat(json).contains("\"name\"")
        assertThat(json).contains("Test Session")
        assertThat(json).contains("\"startTime\"")
        assertThat(json).contains("\"endTime\"")
    }

    @Test
    fun `exportToJson contains readings array`() {
        val json = exporter.exportToJson(session, readings)

        assertThat(json).contains("\"readings\"")
        assertThat(json).contains("\"providerId\"")
        assertThat(json).contains("motion")
        assertThat(json).contains("environment")
    }

    @Test
    fun `exportToJson contains reading values`() {
        val json = exporter.exportToJson(session, readings)

        assertThat(json).contains("accel_x")
        assertThat(json).contains("temp")
        assertThat(json).contains("humidity")
    }

    @Test
    fun `exportToJson produces valid JSON structure`() {
        val json = exporter.exportToJson(session, readings)

        // Should start and end with braces (object)
        assertThat(json.trim()).startsWith("{")
        assertThat(json.trim()).endsWith("}")
    }

    @Test
    fun `exportToJson with empty readings`() {
        val json = exporter.exportToJson(session, emptyList())

        assertThat(json).contains("\"readings\"")
        assertThat(json).contains("[]")
    }

    // --- GPX Tests ---

    @Test
    fun `exportToGpx produces valid GPX header`() {
        val gpx = exporter.exportToGpx(session, readings)

        assertThat(gpx).contains("""<?xml version="1.0" encoding="UTF-8"?>""")
        assertThat(gpx).contains("""<gpx version="1.1" creator="Tricorder"""")
        assertThat(gpx).contains("</gpx>")
    }

    @Test
    fun `exportToGpx contains metadata`() {
        val gpx = exporter.exportToGpx(session, readings)

        assertThat(gpx).contains("<metadata>")
        assertThat(gpx).contains("<name>Test Session</name>")
        assertThat(gpx).contains("</metadata>")
    }

    @Test
    fun `exportToGpx creates waypoints for geo readings`() {
        val gpx = exporter.exportToGpx(session, readings)

        // Only 2 readings have lat/lon
        val wptCount = Regex("<wpt ").findAll(gpx).count()
        assertThat(wptCount).isEqualTo(2)
    }

    @Test
    fun `exportToGpx creates track segment`() {
        val gpx = exporter.exportToGpx(session, readings)

        assertThat(gpx).contains("<trk>")
        assertThat(gpx).contains("<trkseg>")
        assertThat(gpx).contains("<trkpt")
        assertThat(gpx).contains("</trkseg>")
        assertThat(gpx).contains("</trk>")
    }

    @Test
    fun `exportToGpx contains correct coordinates`() {
        val gpx = exporter.exportToGpx(session, readings)

        assertThat(gpx).contains("""lat="47.3769"""")
        assertThat(gpx).contains("""lon="8.5417"""")
        assertThat(gpx).contains("""lat="47.377"""")
        assertThat(gpx).contains("""lon="8.5418"""")
    }

    @Test
    fun `exportToGpx excludes readings without location`() {
        val gpx = exporter.exportToGpx(session, readings)

        // Track points should only be for geo readings
        val trkptCount = Regex("<trkpt ").findAll(gpx).count()
        assertThat(trkptCount).isEqualTo(2)
    }

    @Test
    fun `exportToGpx with no geo readings produces no track`() {
        val noGeoReadings = listOf(
            ReadingEntity(
                id = 1,
                sessionId = "test-session-id",
                timestamp = 1700000001000L,
                providerId = "motion",
                values = """{"x":1.0}""",
                labels = "{}",
                latitude = null,
                longitude = null,
            ),
        )
        val gpx = exporter.exportToGpx(session, noGeoReadings)

        assertThat(gpx).doesNotContain("<trk>")
        assertThat(gpx).doesNotContain("<wpt")
    }

    @Test
    fun `exportToGpx escapes XML special characters in name`() {
        val sessionWithSpecialChars = session.copy(name = "Test <Session> & \"Stuff\"")
        val gpx = exporter.exportToGpx(sessionWithSpecialChars, readings)

        assertThat(gpx).contains("Test &lt;Session&gt; &amp; &quot;Stuff&quot;")
        assertThat(gpx).doesNotContain("<Session>")
    }

    @Test
    fun `exportToGpx contains ISO timestamps`() {
        val gpx = exporter.exportToGpx(session, readings)

        assertThat(gpx).contains("<time>2023-11-14T22:13:21")
    }
}
