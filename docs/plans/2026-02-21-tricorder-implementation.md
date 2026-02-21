# Tricorder App Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a map-centric Android app that uses all available phone sensors plus external APIs to simulate a tricorder, with session recording and detailed instrument views.

**Architecture:** Modular Sensor Engine with three layers — SensorProvider abstraction (Kotlin Flows), MapLibre overlay rendering, and Room-based session management. Hilt DI with multibindings for auto-discovery. See `docs/plans/2026-02-21-tricorder-design.md` for full design.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, MapLibre, Hilt, Room, Retrofit + Moshi, Vico (charts), CameraX, Kotlin Coroutines/Flow

---

## Phase 1: Project Scaffolding

### Task 1: Create Android Project with Gradle Multi-Module Setup

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml` (version catalog)
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/sysop/tricorder/TricorderApp.kt`
- Create: `app/src/main/java/com/sysop/tricorder/MainActivity.kt`

**Step 1: Initialize the Android project**

Create `settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Tricorder"
include(":app")
```

Create `gradle/libs.versions.toml` with all version catalog entries:
```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
compose-bom = "2024.12.01"
compose-compiler = "1.5.15"
hilt = "2.53.1"
room = "2.6.1"
retrofit = "2.11.0"
moshi = "1.15.1"
maplibre = "11.8.2"
vico = "2.0.0-beta.3"
camerax = "1.4.1"
coroutines = "1.9.0"
lifecycle = "2.8.7"
navigation = "2.8.5"
datastore = "1.1.1"
junit = "4.13.2"
truth = "1.4.4"
turbine = "1.2.0"
mockk = "1.13.13"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Lifecycle
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
moshi = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }
moshi-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version = "4.12.0" }

# MapLibre
maplibre = { group = "org.maplibre.gl", name = "android-sdk", version.ref = "maplibre" }

# Charts
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }

# CameraX
camerax-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
camerax-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
camerax-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }

# DataStore
datastore = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

Create root `build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

Create `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

**Step 2: Create app module build file**

Create `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.sysop.tricorder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sysop.tricorder"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling.preview)
    implementation(libs.compose.icons.extended)
    debugImplementation(libs.compose.tooling)

    // Lifecycle
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
```

**Step 3: Create AndroidManifest.xml**

Create `app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Location -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Bluetooth -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <!-- WiFi -->
    <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />

    <!-- Camera & Audio -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Sensors -->
    <uses-permission android:name="android.permission.BODY_SENSORS" />
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

    <!-- Connectivity -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <application
        android:name=".TricorderApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Tricorder"
        android:supportsRtl="true"
        android:theme="@style/Theme.Material3.DayNight.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**Step 4: Create Application and MainActivity**

Create `app/src/main/java/com/sysop/tricorder/TricorderApp.kt`:
```kotlin
package com.sysop.tricorder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TricorderApp : Application()
```

Create `app/src/main/java/com/sysop/tricorder/MainActivity.kt`:
```kotlin
package com.sysop.tricorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Text("Tricorder")
        }
    }
}
```

**Step 5: Verify the project builds**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add -A
git commit -m "chore: scaffold Android project with Gradle multi-module setup"
```

---

## Phase 2: Core Model & Sensor API

### Task 2: Define Core Domain Models

**Files:**
- Create: `core/model/build.gradle.kts`
- Create: `core/model/src/main/java/com/sysop/tricorder/core/model/SensorCategory.kt`
- Create: `core/model/src/main/java/com/sysop/tricorder/core/model/SensorReading.kt`
- Create: `core/model/src/main/java/com/sysop/tricorder/core/model/SensorAvailability.kt`
- Create: `core/model/src/main/java/com/sysop/tricorder/core/model/MapOverlayConfig.kt`
- Create: `core/model/src/main/java/com/sysop/tricorder/core/model/Session.kt`
- Modify: `settings.gradle.kts` (add module include)

**Step 1: Create the core/model module**

Add to `settings.gradle.kts`:
```kotlin
include(":core:model")
```

Create `core/model/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.sysop.tricorder.core.model"
    compileSdk = 35
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
```

**Step 2: Create SensorCategory**

```kotlin
package com.sysop.tricorder.core.model

enum class SensorCategory(val displayName: String, val icon: String) {
    MOTION("Motion", "compass"),
    ENVIRONMENT("Environment", "thermometer"),
    LOCATION("Location", "satellite"),
    RF("RF & Connectivity", "radio"),
    AUDIO("Audio", "mic"),
    CAMERA("Camera", "camera"),
    WEATHER("Weather", "cloud"),
    AIR_QUALITY("Air Quality", "wind"),
    AVIATION("Aviation", "plane"),
    SEISMIC("Seismic", "activity"),
    RADIATION("Radiation", "alert-triangle"),
    SPACE("Space", "star"),
    TIDES("Tides", "waves"),
}
```

**Step 3: Create SensorReading**

```kotlin
package com.sysop.tricorder.core.model

import java.time.Instant

data class SensorReading(
    val providerId: String,
    val category: SensorCategory,
    val timestamp: Instant,
    val values: Map<String, Double>,
    val labels: Map<String, String> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
```

**Step 4: Create SensorAvailability**

```kotlin
package com.sysop.tricorder.core.model

enum class SensorAvailability {
    AVAILABLE,
    UNAVAILABLE,
    REQUIRES_PERMISSION,
    REQUIRES_API_KEY,
}
```

**Step 5: Create MapOverlayConfig**

```kotlin
package com.sysop.tricorder.core.model

data class MapOverlayConfig(
    val type: OverlayType,
    val colorScheme: List<Long> = emptyList(),
    val opacity: Float = 0.7f,
)

enum class OverlayType {
    HEATMAP,
    MARKERS,
    CIRCLES,
    POLYLINE,
    VECTOR_FIELD,
    SKY_PLOT,
}
```

**Step 6: Create Session model**

```kotlin
package com.sysop.tricorder.core.model

import java.time.Instant
import java.util.UUID

data class Session(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val startTime: Instant,
    val endTime: Instant? = null,
    val latitude: Double,
    val longitude: Double,
    val activeProviders: List<String>,
)

data class TimestampedReading(
    val sessionId: UUID,
    val timestamp: Instant,
    val providerId: String,
    val values: Map<String, Double>,
    val labels: Map<String, String> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
```

**Step 7: Verify build**

Run: `./gradlew :core:model:build`
Expected: BUILD SUCCESSFUL

**Step 8: Commit**

```bash
git add core/model/ settings.gradle.kts
git commit -m "feat: add core domain models (SensorReading, Session, SensorCategory)"
```

---

### Task 3: Define SensorProvider Interface and SensorRegistry

**Files:**
- Create: `core/sensor-api/build.gradle.kts`
- Create: `core/sensor-api/src/main/java/com/sysop/tricorder/core/sensorapi/SensorProvider.kt`
- Create: `core/sensor-api/src/main/java/com/sysop/tricorder/core/sensorapi/SensorRegistry.kt`
- Create: `core/sensor-api/src/test/java/com/sysop/tricorder/core/sensorapi/SensorRegistryTest.kt`
- Modify: `settings.gradle.kts`

**Step 1: Create the module**

Add to `settings.gradle.kts`:
```kotlin
include(":core:sensor-api")
```

Create `core/sensor-api/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.sysop.tricorder.core.sensorapi"
    compileSdk = 35
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api(project(":core:model"))
    implementation(libs.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
}
```

**Step 2: Write the failing test for SensorRegistry**

Create `core/sensor-api/src/test/java/com/sysop/tricorder/core/sensorapi/SensorRegistryTest.kt`:
```kotlin
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
```

**Step 3: Run test to verify it fails**

Run: `./gradlew :core:sensor-api:test`
Expected: FAIL — classes not found

**Step 4: Create SensorProvider interface**

Create `core/sensor-api/src/main/java/com/sysop/tricorder/core/sensorapi/SensorProvider.kt`:
```kotlin
package com.sysop.tricorder.core.sensorapi

import com.sysop.tricorder.core.model.*
import kotlinx.coroutines.flow.Flow

interface SensorProvider {
    val id: String
    val name: String
    val category: SensorCategory
    fun availability(): SensorAvailability
    fun readings(): Flow<SensorReading>
    fun mapOverlay(): MapOverlayConfig?
}
```

**Step 5: Create SensorRegistry**

Create `core/sensor-api/src/main/java/com/sysop/tricorder/core/sensorapi/SensorRegistry.kt`:
```kotlin
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
```

Note: Add Hilt dependency to `core/sensor-api/build.gradle.kts`:
```kotlin
implementation(libs.hilt.android)
```

**Step 6: Run tests to verify they pass**

Run: `./gradlew :core:sensor-api:test`
Expected: 3 tests PASS

**Step 7: Commit**

```bash
git add core/sensor-api/ settings.gradle.kts
git commit -m "feat: add SensorProvider interface and SensorRegistry with tests"
```

---

## Phase 3: Data Layer

### Task 4: Room Database for Sessions

**Files:**
- Create: `core/database/build.gradle.kts`
- Create: `core/database/src/main/java/com/sysop/tricorder/core/database/TricorderDatabase.kt`
- Create: `core/database/src/main/java/com/sysop/tricorder/core/database/entity/SessionEntity.kt`
- Create: `core/database/src/main/java/com/sysop/tricorder/core/database/entity/ReadingEntity.kt`
- Create: `core/database/src/main/java/com/sysop/tricorder/core/database/dao/SessionDao.kt`
- Create: `core/database/src/main/java/com/sysop/tricorder/core/database/converter/Converters.kt`
- Modify: `settings.gradle.kts`

**Step 1: Create the database module**

Add to `settings.gradle.kts`:
```kotlin
include(":core:database")
```

Create `core/database/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sysop.tricorder.core.database"
    compileSdk = 35
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api(project(":core:model"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.moshi)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
```

**Step 2: Create Room entities**

Create `SessionEntity.kt`:
```kotlin
package com.sysop.tricorder.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val startTime: Long,
    val endTime: Long?,
    val latitude: Double,
    val longitude: Double,
    val activeProviders: String, // JSON array
)
```

Create `ReadingEntity.kt`:
```kotlin
package com.sysop.tricorder.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "readings",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sessionId"), Index("timestamp")],
)
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestamp: Long,
    val providerId: String,
    val values: String, // JSON map
    val labels: String, // JSON map
    val latitude: Double?,
    val longitude: Double?,
)
```

**Step 3: Create DAO**

Create `SessionDao.kt`:
```kotlin
package com.sysop.tricorder.core.database.dao

import androidx.room.*
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSession(id: String): SessionEntity?

    @Insert
    suspend fun insertReading(reading: ReadingEntity)

    @Insert
    suspend fun insertReadings(readings: List<ReadingEntity>)

    @Query("SELECT * FROM readings WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getReadingsForSession(sessionId: String): Flow<List<ReadingEntity>>

    @Query("DELETE FROM sessions WHERE startTime < :before")
    suspend fun deleteSessionsBefore(before: Long)
}
```

**Step 4: Create type converters**

Create `Converters.kt`:
```kotlin
package com.sysop.tricorder.core.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {
    private val moshi = Moshi.Builder().build()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)
    private val stringMapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    private val stringMapAdapter = moshi.adapter<Map<String, String>>(stringMapType)
    private val doubleMapType = Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType)
    private val doubleMapAdapter = moshi.adapter<Map<String, Double>>(doubleMapType)

    @TypeConverter
    fun fromStringList(value: List<String>): String = stringListAdapter.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = stringListAdapter.fromJson(value) ?: emptyList()

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String = stringMapAdapter.toJson(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> = stringMapAdapter.fromJson(value) ?: emptyMap()

    @TypeConverter
    fun fromDoubleMap(value: Map<String, Double>): String = doubleMapAdapter.toJson(value)

    @TypeConverter
    fun toDoubleMap(value: String): Map<String, Double> = doubleMapAdapter.fromJson(value) ?: emptyMap()
}
```

**Step 5: Create the database**

Create `TricorderDatabase.kt`:
```kotlin
package com.sysop.tricorder.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sysop.tricorder.core.database.converter.Converters
import com.sysop.tricorder.core.database.dao.SessionDao
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, ReadingEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TricorderDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
```

**Step 6: Verify build**

Run: `./gradlew :core:database:build`
Expected: BUILD SUCCESSFUL

**Step 7: Commit**

```bash
git add core/database/ settings.gradle.kts
git commit -m "feat: add Room database with Session and Reading entities"
```

---

### Task 5: Network Layer (Retrofit Base)

**Files:**
- Create: `core/network/build.gradle.kts`
- Create: `core/network/src/main/java/com/sysop/tricorder/core/network/di/NetworkModule.kt`
- Modify: `settings.gradle.kts`

**Step 1: Create network module**

Add to `settings.gradle.kts`:
```kotlin
include(":core:network")
```

Create `core/network/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.sysop.tricorder.core.network"
    compileSdk = 35
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)
    implementation(libs.okhttp.logging)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

**Step 2: Create Hilt NetworkModule**

Create `core/network/src/main/java/com/sysop/tricorder/core/network/di/NetworkModule.kt`:
```kotlin
package com.sysop.tricorder.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()
}
```

**Step 3: Verify build**

Run: `./gradlew :core:network:build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add core/network/ settings.gradle.kts
git commit -m "feat: add network module with Retrofit/Moshi/OkHttp base setup"
```

---

### Task 6: DataStore Preferences Module

**Files:**
- Create: `core/datastore/build.gradle.kts`
- Create: `core/datastore/src/main/java/com/sysop/tricorder/core/datastore/TricorderPreferences.kt`
- Modify: `settings.gradle.kts`

**Step 1: Create datastore module**

Add `include(":core:datastore")` to `settings.gradle.kts`.

Create `core/datastore/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.sysop.tricorder.core.datastore"
    compileSdk = 35
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.datastore)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

**Step 2: Create TricorderPreferences**

```kotlin
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
```

**Step 3: Verify build and commit**

Run: `./gradlew :core:datastore:build`

```bash
git add core/datastore/ settings.gradle.kts
git commit -m "feat: add DataStore preferences module"
```

---

## Phase 4: First Hardware Sensor Providers

### Task 7: Motion Sensor Provider (Accelerometer, Gyroscope, Magnetometer)

**Files:**
- Create: `sensor/motion/build.gradle.kts`
- Create: `sensor/motion/src/main/java/com/sysop/tricorder/sensor/motion/MotionSensorProvider.kt`
- Create: `sensor/motion/src/main/java/com/sysop/tricorder/sensor/motion/di/MotionModule.kt`
- Create: `sensor/motion/src/test/java/com/sysop/tricorder/sensor/motion/MotionSensorProviderTest.kt`
- Modify: `settings.gradle.kts`

**Step 1: Create motion sensor module**

Add `include(":sensor:motion")` to `settings.gradle.kts`.

Create `sensor/motion/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.sysop.tricorder.sensor.motion"
    compileSdk = 35
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core:sensor-api"))
    implementation(libs.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
```

**Step 2: Write the failing test**

```kotlin
package com.sysop.tricorder.sensor.motion

import com.google.common.truth.Truth.assertThat
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorAvailability
import com.sysop.tricorder.core.model.OverlayType
import org.junit.Test

class MotionSensorProviderTest {

    @Test
    fun `provider has correct metadata`() {
        // We can't test with real SensorManager in unit tests,
        // but we can verify the provider's static properties
        val provider = MotionSensorProvider(sensorManager = null)
        assertThat(provider.id).isEqualTo("motion")
        assertThat(provider.name).isEqualTo("Motion & Orientation")
        assertThat(provider.category).isEqualTo(SensorCategory.MOTION)
    }

    @Test
    fun `unavailable when no sensor manager`() {
        val provider = MotionSensorProvider(sensorManager = null)
        assertThat(provider.availability()).isEqualTo(SensorAvailability.UNAVAILABLE)
    }

    @Test
    fun `map overlay is vector field type`() {
        val provider = MotionSensorProvider(sensorManager = null)
        val overlay = provider.mapOverlay()
        assertThat(overlay).isNotNull()
        assertThat(overlay!!.type).isEqualTo(OverlayType.VECTOR_FIELD)
    }
}
```

**Step 3: Run test to verify it fails**

Run: `./gradlew :sensor:motion:test`
Expected: FAIL

**Step 4: Implement MotionSensorProvider**

```kotlin
package com.sysop.tricorder.sensor.motion

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject

class MotionSensorProvider @Inject constructor(
    private val sensorManager: SensorManager?,
) : SensorProvider {

    override val id = "motion"
    override val name = "Motion & Orientation"
    override val category = SensorCategory.MOTION

    override fun availability(): SensorAvailability {
        if (sensorManager == null) return SensorAvailability.UNAVAILABLE
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return if (accel != null) SensorAvailability.AVAILABLE else SensorAvailability.UNAVAILABLE
    }

    override fun readings(): Flow<SensorReading> = callbackFlow {
        val sm = sensorManager ?: run { close(); return@callbackFlow }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = mutableMapOf<String, Double>()
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        values["accel_x"] = event.values[0].toDouble()
                        values["accel_y"] = event.values[1].toDouble()
                        values["accel_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        values["gyro_x"] = event.values[0].toDouble()
                        values["gyro_y"] = event.values[1].toDouble()
                        values["gyro_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        values["mag_x"] = event.values[0].toDouble()
                        values["mag_y"] = event.values[1].toDouble()
                        values["mag_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        values["rot_x"] = event.values[0].toDouble()
                        values["rot_y"] = event.values[1].toDouble()
                        values["rot_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_STEP_COUNTER -> {
                        values["steps"] = event.values[0].toDouble()
                    }
                }
                trySend(SensorReading(
                    providerId = id,
                    category = category,
                    timestamp = Instant.now(),
                    values = values,
                ))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val sensors = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_STEP_COUNTER,
        )
        sensors.forEach { type ->
            sm.getDefaultSensor(type)?.let { sensor ->
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        awaitClose {
            sm.unregisterListener(listener)
        }
    }

    override fun mapOverlay() = MapOverlayConfig(
        type = OverlayType.VECTOR_FIELD,
    )
}
```

**Step 5: Create Hilt module**

```kotlin
package com.sysop.tricorder.sensor.motion.di

import android.content.Context
import android.hardware.SensorManager
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.motion.MotionSensorProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class MotionModule {

    @Binds
    @IntoSet
    abstract fun bindMotionProvider(impl: MotionSensorProvider): SensorProvider

    companion object {
        @Provides
        fun provideSensorManager(@ApplicationContext context: Context): SensorManager? =
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
}
```

**Step 6: Run tests**

Run: `./gradlew :sensor:motion:test`
Expected: 3 tests PASS

**Step 7: Commit**

```bash
git add sensor/motion/ settings.gradle.kts
git commit -m "feat: add motion sensor provider (accel, gyro, mag, rotation, steps)"
```

---

### Task 8: Environment Sensor Provider (Barometer, Light, Proximity)

**Files:**
- Create: `sensor/environment/build.gradle.kts`
- Create: `sensor/environment/src/main/java/com/sysop/tricorder/sensor/environment/EnvironmentSensorProvider.kt`
- Create: `sensor/environment/src/main/java/com/sysop/tricorder/sensor/environment/di/EnvironmentModule.kt`
- Create: `sensor/environment/src/test/java/com/sysop/tricorder/sensor/environment/EnvironmentSensorProviderTest.kt`
- Modify: `settings.gradle.kts`

Follow same pattern as Task 7. The provider reads `TYPE_PRESSURE`, `TYPE_LIGHT`, and `TYPE_PROXIMITY`. Values emitted: `pressure_hpa`, `altitude_m` (computed from pressure using `SensorManager.getAltitude`), `light_lux`, `proximity_cm`. Map overlay type: `HEATMAP`. Category: `ENVIRONMENT`.

**Step 1-7:** Same structure as Task 7 — write test, implement, create Hilt module, verify, commit.

```bash
git commit -m "feat: add environment sensor provider (barometer, light, proximity)"
```

---

### Task 9: Location / GNSS Provider

**Files:**
- Create: `sensor/location/build.gradle.kts`
- Create: `sensor/location/src/main/java/com/sysop/tricorder/sensor/location/LocationProvider.kt`
- Create: `sensor/location/src/main/java/com/sysop/tricorder/sensor/location/di/LocationModule.kt`
- Modify: `settings.gradle.kts`

This provider uses `FusedLocationProviderClient` (Google Play Services) or `LocationManager` as fallback. Emits: `latitude`, `longitude`, `altitude`, `speed`, `bearing`, `accuracy`, `satellite_count`. Map overlay type: `MARKERS`. Category: `LOCATION`.

Add `play-services-location` dependency.

For GNSS satellite info, use `GnssStatus` callback to provide satellite constellation data (PRN, elevation, azimuth, CN0, constellation type). This feeds the sky plot detail view.

```bash
git commit -m "feat: add location/GNSS provider with satellite info"
```

---

### Task 10: RF Scanner Provider (WiFi, BLE, Cellular)

**Files:**
- Create: `sensor/rf/build.gradle.kts`
- Create: `sensor/rf/src/main/java/com/sysop/tricorder/sensor/rf/WifiScanProvider.kt`
- Create: `sensor/rf/src/main/java/com/sysop/tricorder/sensor/rf/BleScanProvider.kt`
- Create: `sensor/rf/src/main/java/com/sysop/tricorder/sensor/rf/CellularProvider.kt`
- Create: `sensor/rf/src/main/java/com/sysop/tricorder/sensor/rf/di/RfModule.kt`
- Modify: `settings.gradle.kts`

Three separate providers sharing the RF category:

**WifiScanProvider** (`id: "wifi-scan"`): Uses `WifiManager.startScan()` and `BroadcastReceiver` for `SCAN_RESULTS_AVAILABLE_ACTION`. Emits per-network readings with `ssid`, `bssid`, `rssi`, `frequency`, `channel_width`. Map overlay: `CIRCLES`. Requires `NEARBY_WIFI_DEVICES` permission.

**BleScanProvider** (`id: "ble-scan"`): Uses `BluetoothLeScanner.startScan()`. Emits per-device readings with `device_name`, `mac_address`, `rssi`, `tx_power`. Map overlay: `MARKERS`. Requires `BLUETOOTH_SCAN`.

**CellularProvider** (`id: "cellular"`): Uses `TelephonyManager` and `CellInfoCallback`. Emits `cell_id`, `mcc`, `mnc`, `rssi`, `rsrp`, `rsrq`, `signal_level`, `network_type`. Map overlay: `CIRCLES`.

```bash
git commit -m "feat: add RF sensor providers (WiFi, BLE, cellular)"
```

---

### Task 11: Audio Analyzer Provider

**Files:**
- Create: `sensor/audio/build.gradle.kts`
- Create: `sensor/audio/src/main/java/com/sysop/tricorder/sensor/audio/AudioAnalyzerProvider.kt`
- Create: `sensor/audio/src/main/java/com/sysop/tricorder/sensor/audio/FftProcessor.kt`
- Create: `sensor/audio/src/main/java/com/sysop/tricorder/sensor/audio/di/AudioModule.kt`
- Create: `sensor/audio/src/test/java/com/sysop/tricorder/sensor/audio/FftProcessorTest.kt`
- Modify: `settings.gradle.kts`

Uses `AudioRecord` with `ENCODING_PCM_FLOAT`, sample rate 44100 Hz, buffer size from `AudioRecord.getMinBufferSize()`.

**FftProcessor**: Takes PCM samples, applies Hanning window, computes FFT (use Apache Commons Math `FastFourierTransformer` or implement Cooley-Tukey). Returns magnitude spectrum.

Emitted values: `db_spl` (sound pressure level), `peak_frequency_hz`, `spectrum` (as a comma-separated string of magnitudes in the labels map for the detail view to parse).

Map overlay type: `HEATMAP` (shows dB level as color around user position).

**Test FftProcessor** with known sine wave input — verify peak at expected frequency bin.

```bash
git commit -m "feat: add audio analyzer provider with FFT processing"
```

---

### Task 12: Camera Analysis Provider

**Files:**
- Create: `sensor/camera/build.gradle.kts`
- Create: `sensor/camera/src/main/java/com/sysop/tricorder/sensor/camera/CameraAnalysisProvider.kt`
- Create: `sensor/camera/src/main/java/com/sysop/tricorder/sensor/camera/ColorAnalyzer.kt`
- Create: `sensor/camera/src/main/java/com/sysop/tricorder/sensor/camera/di/CameraModule.kt`
- Modify: `settings.gradle.kts`

Uses CameraX `ImageAnalysis` use case. `ColorAnalyzer` implements `ImageAnalysis.Analyzer`, processes each frame to extract:
- `avg_r`, `avg_g`, `avg_b` (average color of center region)
- `brightness` (overall luminance)
- `dominant_hue` (most common hue bucket from HSV histogram)

For heart rate estimation (PPG): when user places finger on camera, detect redness pulsation, apply bandpass filter (0.7-4 Hz), find peaks to estimate BPM. Emits `heart_rate_bpm` when in PPG mode.

This provider is not always-on — it activates when the user opens the camera detail view.

```bash
git commit -m "feat: add camera analysis provider with color analysis and PPG"
```

---

## Phase 5: External API Providers

### Task 13: Weather API Provider

**Files:**
- Create: `sensor/weather/build.gradle.kts`
- Create: `sensor/weather/src/main/java/com/sysop/tricorder/sensor/weather/api/OpenMeteoApi.kt`
- Create: `sensor/weather/src/main/java/com/sysop/tricorder/sensor/weather/api/OpenMeteoResponse.kt`
- Create: `sensor/weather/src/main/java/com/sysop/tricorder/sensor/weather/WeatherProvider.kt`
- Create: `sensor/weather/src/main/java/com/sysop/tricorder/sensor/weather/di/WeatherModule.kt`
- Create: `sensor/weather/src/test/java/com/sysop/tricorder/sensor/weather/WeatherProviderTest.kt`
- Modify: `settings.gradle.kts`

Uses **Open-Meteo** (free, no API key) as primary weather source. Falls back to OpenWeatherMap if user provides a key.

**Open-Meteo API:**
```
GET https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,uv_index,surface_pressure
```

Create Retrofit interface, Moshi response model. Provider polls every 5 minutes (configurable). Emits: `temperature_c`, `humidity_pct`, `wind_speed_kmh`, `wind_direction_deg`, `uv_index`, `pressure_hpa`.

Map overlay: `HEATMAP` (temperature colored).

**Test:** Use MockWebServer to verify API parsing and reading emission.

```bash
git commit -m "feat: add weather provider using Open-Meteo API"
```

---

### Task 14: Air Quality API Provider

**Files:**
- Create: `sensor/airquality/build.gradle.kts`
- Create: `sensor/airquality/src/main/java/com/sysop/tricorder/sensor/airquality/api/WaqiApi.kt`
- Create: `sensor/airquality/src/main/java/com/sysop/tricorder/sensor/airquality/AirQualityProvider.kt`
- Create: `sensor/airquality/src/main/java/com/sysop/tricorder/sensor/airquality/di/AirQualityModule.kt`
- Modify: `settings.gradle.kts`

Uses **WAQI (World Air Quality Index)** API. Free tier available.

```
GET https://api.waqi.info/feed/geo:{lat};{lon}/?token={key}
```

Emits: `aqi`, `pm25`, `pm10`, `o3`, `no2`, `so2`, `co`. Map overlay: `HEATMAP` (AQI colored green->yellow->red->purple).

```bash
git commit -m "feat: add air quality provider using WAQI API"
```

---

### Task 15: Aviation Provider (Aircraft Tracking)

**Files:**
- Create: `sensor/aviation/build.gradle.kts`
- Create: `sensor/aviation/src/main/java/com/sysop/tricorder/sensor/aviation/api/OpenSkyApi.kt`
- Create: `sensor/aviation/src/main/java/com/sysop/tricorder/sensor/aviation/AviationProvider.kt`
- Create: `sensor/aviation/src/main/java/com/sysop/tricorder/sensor/aviation/di/AviationModule.kt`
- Modify: `settings.gradle.kts`

Uses **OpenSky Network** API (free, no key for limited use).

```
GET https://opensky-network.org/api/states/all?lamin={lat-1}&lomin={lon-1}&lamax={lat+1}&lomax={lon+1}
```

Emits per-aircraft readings: `callsign`, `origin_country` (labels), `longitude`, `latitude`, `altitude_m`, `velocity_ms`, `heading_deg`, `vertical_rate`. Polls every 10 seconds.

Map overlay: `MARKERS` (plane icons with heading rotation and altitude labels).

```bash
git commit -m "feat: add aviation provider using OpenSky Network API"
```

---

### Task 16: Seismic Provider (USGS Earthquakes)

**Files:**
- Create: `sensor/seismic/build.gradle.kts`
- Create: `sensor/seismic/src/main/java/com/sysop/tricorder/sensor/seismic/api/UsgsApi.kt`
- Create: `sensor/seismic/src/main/java/com/sysop/tricorder/sensor/seismic/SeismicProvider.kt`
- Create: `sensor/seismic/src/main/java/com/sysop/tricorder/sensor/seismic/di/SeismicModule.kt`
- Modify: `settings.gradle.kts`

Uses **USGS Earthquake API** (free, no key).

```
GET https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime={7daysago}&minmagnitude=2.5&latitude={lat}&longitude={lon}&maxradiuskm=500
```

Emits per-earthquake readings: `magnitude`, `depth_km`, `latitude`, `longitude`, `place` (label), `time_epoch`.

Map overlay: `CIRCLES` (sized by magnitude, colored by recency).

Polls every 5 minutes.

```bash
git commit -m "feat: add seismic provider using USGS earthquake API"
```

---

### Task 17: Radiation Provider (Safecast)

**Files:**
- Create: `sensor/radiation/build.gradle.kts`
- Create: `sensor/radiation/src/main/java/com/sysop/tricorder/sensor/radiation/api/SafecastApi.kt`
- Create: `sensor/radiation/src/main/java/com/sysop/tricorder/sensor/radiation/RadiationProvider.kt`
- Create: `sensor/radiation/src/main/java/com/sysop/tricorder/sensor/radiation/di/RadiationModule.kt`
- Modify: `settings.gradle.kts`

Uses **Safecast API** (free, community data).

```
GET https://api.safecast.org/measurements.json?latitude={lat}&longitude={lon}&distance=50
```

Emits: `cpm` (counts per minute), `usv_h` (microsieverts/hour), `latitude`, `longitude`. Map overlay: `HEATMAP`.

```bash
git commit -m "feat: add radiation provider using Safecast API"
```

---

### Task 18: Space / Satellite Tracking Provider

**Files:**
- Create: `sensor/space/build.gradle.kts`
- Create: `sensor/space/src/main/java/com/sysop/tricorder/sensor/space/api/N2yoApi.kt`
- Create: `sensor/space/src/main/java/com/sysop/tricorder/sensor/space/SpaceProvider.kt`
- Create: `sensor/space/src/main/java/com/sysop/tricorder/sensor/space/di/SpaceModule.kt`
- Modify: `settings.gradle.kts`

Uses **N2YO API** (free tier, key required).

```
GET https://api.n2yo.com/rest/v1/satellite/above/{lat}/{lon}/0/70/0/&apiKey={key}
```

Emits per-satellite: `norad_id`, `sat_name` (label), `latitude`, `longitude`, `altitude_km`, `azimuth`, `elevation`.

Map overlay: `MARKERS` (satellite icons on map).

Also include ISS tracking as a special case (NORAD ID 25544).

```bash
git commit -m "feat: add space/satellite tracking provider using N2YO API"
```

---

### Task 19: RF Intelligence Providers (OpenCelliD, WiGLE)

**Files:**
- Create: `sensor/rfintel/build.gradle.kts`
- Create: `sensor/rfintel/src/main/java/com/sysop/tricorder/sensor/rfintel/api/OpenCellidApi.kt`
- Create: `sensor/rfintel/src/main/java/com/sysop/tricorder/sensor/rfintel/CellTowerDbProvider.kt`
- Create: `sensor/rfintel/src/main/java/com/sysop/tricorder/sensor/rfintel/di/RfIntelModule.kt`
- Modify: `settings.gradle.kts`

**OpenCelliD**: Cross-references locally detected cell towers with database to show known tower locations on map. Requires API key.

```
GET https://opencellid.org/cell/get?key={key}&mcc={mcc}&mnc={mnc}&lac={lac}&cellid={cellid}
```

Emits: `tower_lat`, `tower_lon`, `range_m`, `samples` per cell tower detected by the CellularProvider.

Map overlay: `CIRCLES` (tower position + estimated range).

```bash
git commit -m "feat: add RF intelligence providers (OpenCelliD cell tower DB)"
```

---

### Task 20: Tides & Water Provider (NOAA)

**Files:**
- Create: `sensor/tides/build.gradle.kts`
- Create: `sensor/tides/src/main/java/com/sysop/tricorder/sensor/tides/api/NoaaTidesApi.kt`
- Create: `sensor/tides/src/main/java/com/sysop/tricorder/sensor/tides/TidesProvider.kt`
- Create: `sensor/tides/src/main/java/com/sysop/tricorder/sensor/tides/di/TidesModule.kt`
- Modify: `settings.gradle.kts`

Uses **NOAA CO-OPS API** (free, no key).

```
GET https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?station={nearest}&product=predictions&datum=MLLW&time_zone=lst_ldt&units=metric&interval=hilo&format=json&begin_date={today}&range=48
```

First finds nearest station, then fetches predictions. Emits: `water_level_m`, `tide_type` (label: "H" or "L"), `prediction_time`.

Map overlay: `MARKERS` at station locations.

```bash
git commit -m "feat: add tides/water provider using NOAA CO-OPS API"
```

---

## Phase 6: Map & Main UI

### Task 21: UI Common Module (Theme, Shared Composables)

**Files:**
- Create: `core/ui-common/build.gradle.kts`
- Create: `core/ui-common/src/main/java/com/sysop/tricorder/core/ui/theme/TricorderTheme.kt`
- Create: `core/ui-common/src/main/java/com/sysop/tricorder/core/ui/theme/Color.kt`
- Create: `core/ui-common/src/main/java/com/sysop/tricorder/core/ui/components/SensorReadoutChip.kt`
- Create: `core/ui-common/src/main/java/com/sysop/tricorder/core/ui/components/LiveGraph.kt`
- Modify: `settings.gradle.kts`

**TricorderTheme**: Material 3 dark theme with custom color scheme. Dark background (deep navy/black), accent colors per sensor category:
- Motion: cyan
- Environment: green
- Location: blue
- RF: orange
- Audio: purple
- Camera: pink
- Weather: yellow
- Air Quality: teal
- Aviation: sky blue
- Seismic: red
- Radiation: amber
- Space: indigo

**SensorReadoutChip**: Small composable showing icon + value + unit. Used in the quick readout bar.

**LiveGraph**: Wrapper around Vico chart for real-time scrolling line graph. Takes a `Flow<Float>` and displays the last N values.

```bash
git commit -m "feat: add UI common module with theme and shared composables"
```

---

### Task 22: Map Screen with MapLibre

**Files:**
- Create: `feature/map/build.gradle.kts`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/MapScreen.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/MapViewModel.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/overlay/OverlayRenderer.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/overlay/HeatmapOverlay.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/overlay/MarkerOverlay.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/overlay/CircleOverlay.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/components/QuickReadoutBar.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/components/CategoryTabs.kt`
- Create: `feature/map/src/main/java/com/sysop/tricorder/feature/map/components/SensorDetailSheet.kt`
- Modify: `settings.gradle.kts`

**MapScreen**: Main composable. Full-screen MapLibre map with:
- User location dot
- Overlay layers from active providers
- Bottom `CategoryTabs` row
- `QuickReadoutBar` above tabs
- `SensorDetailSheet` (ModalBottomSheet) when a reading is tapped
- Top bar with hamburger menu + record button

**MapViewModel**: Injects `SensorRegistry`. Exposes:
- `activeCategories: StateFlow<Set<SensorCategory>>` — which tabs are toggled on
- `readings: StateFlow<Map<String, SensorReading>>` — latest reading per provider
- `toggleCategory(category)` — add/remove category
- Collects readings from all active providers and merges into a single flow

**OverlayRenderer interface**: Each overlay type implements `render(map, readings)`. The `OverlayRendererRegistry` maps `OverlayType` -> `OverlayRenderer`.

MapLibre setup:
- Use OpenFreeMap style URL (free, no key): `https://tiles.openfreemap.org/styles/liberty`
- Center on user location
- Enable rotation and tilt

```bash
git commit -m "feat: add map screen with MapLibre, overlays, category tabs, and readout bar"
```

---

### Task 23: App Navigation Setup

**Files:**
- Create: `app/src/main/java/com/sysop/tricorder/navigation/TricorderNavHost.kt`
- Create: `app/src/main/java/com/sysop/tricorder/navigation/Screen.kt`
- Modify: `app/src/main/java/com/sysop/tricorder/MainActivity.kt`
- Modify: `app/build.gradle.kts` (add feature module dependencies)

**Screen sealed class**: Defines routes:
- `Screen.Map` — main map screen (home)
- `Screen.Detail(category: SensorCategory)` — full-screen detail view
- `Screen.Sessions` — session list
- `Screen.SessionReplay(sessionId: String)` — session replay
- `Screen.Settings` — settings screen

**TricorderNavHost**: NavHost composable with route definitions. Map screen is start destination.

Wire up `MainActivity` to use `TricorderTheme` + `TricorderNavHost`.

```bash
git commit -m "feat: add navigation with NavHost and route definitions"
```

---

## Phase 7: Detail Views

### Task 24: Compass / Magnetometer Detail View

**Files:**
- Create: `feature/detail/build.gradle.kts`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/compass/CompassScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/compass/CompassViewModel.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/compass/CompassRose.kt`
- Modify: `settings.gradle.kts`

**CompassRose**: Custom Canvas composable that draws:
- Outer circle with degree markings (N, NE, E, SE, S, SW, W, NW)
- Rotating needle showing magnetic north
- True north indicator (if declination available from `GeomagneticField`)
- Tilt-compensated using rotation vector sensor
- Field strength gauge (micro-Tesla) as arc indicator

**CompassViewModel**: Subscribes to motion provider's magnetometer + rotation vector readings. Computes heading using `SensorManager.getRotationMatrixFromVector` + `SensorManager.getOrientation`.

```bash
git commit -m "feat: add compass/magnetometer detail view"
```

---

### Task 25: Audio Spectrum Detail View

**Files:**
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/audio/AudioSpectrumScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/audio/AudioSpectrumViewModel.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/audio/SpectrogramCanvas.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/audio/WaveformCanvas.kt`

**SpectrogramCanvas**: Waterfall spectrogram drawn on Canvas. Each column is one FFT frame, color-mapped (dark blue -> cyan -> yellow -> red). Scrolls left as new data arrives.

**WaveformCanvas**: Real-time PCM waveform oscilloscope.

**Screen layout**:
- Top: dB meter with peak hold indicator
- Middle: Spectrogram (large)
- Bottom: Waveform
- Floating: Frequency cursor (tap spectrogram to read frequency)

```bash
git commit -m "feat: add audio spectrum detail view with spectrogram and waveform"
```

---

### Task 26: RF Scanner Detail View

**Files:**
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/rf/RfScannerScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/rf/RfScannerViewModel.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/rf/WifiListView.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/rf/BleListView.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/rf/ChannelChart.kt`

**Tabs**: WiFi | Bluetooth | Cellular

**WiFi tab**: List of detected networks sorted by signal strength. Each item shows SSID, BSSID, RSSI (bar indicator), channel, frequency band (2.4/5/6 GHz). Expandable to show signal strength graph over time. Channel utilization chart at top showing overlapping channels.

**Bluetooth tab**: List of BLE devices with name, MAC, RSSI, estimated distance. Radar-style visualization option.

**Cellular tab**: Current cell info, neighbor cells, signal strength graph.

```bash
git commit -m "feat: add RF scanner detail view with WiFi, BLE, and cellular tabs"
```

---

### Task 27: Motion / IMU Detail View

**Files:**
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/motion/MotionScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/motion/MotionViewModel.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/motion/OrientationCube.kt`

**OrientationCube**: 3D wireframe cube rendered on Canvas that rotates based on phone orientation (using rotation vector). Shows pitch, roll, yaw numerically.

**Live graphs** (using Vico): Three-panel layout showing accelerometer X/Y/Z, gyroscope X/Y/Z, and magnetometer X/Y/Z as scrolling line charts.

**Step counter**: Large number display with daily graph.

```bash
git commit -m "feat: add motion/IMU detail view with 3D orientation and live graphs"
```

---

### Task 28: Barometer / Altimeter Detail View

**Files:**
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/environment/BarometerScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/environment/BarometerViewModel.kt`

**Layout**:
- Large altitude readout (computed from barometric pressure)
- Pressure trend graph (last 3 hours) with weather prediction arrows (rising = fair, falling = storm)
- Current pressure in hPa
- Sea-level pressure option (user enters elevation for calibration)
- Light sensor reading with lux value and visual brightness indicator

```bash
git commit -m "feat: add barometer/altimeter detail view with pressure trends"
```

---

### Task 29: GNSS Sky Plot Detail View

**Files:**
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/gnss/GnssSkyPlotScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/gnss/GnssSkyPlotViewModel.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/gnss/SkyPlotCanvas.kt`

**SkyPlotCanvas**: Polar plot showing satellite positions. Center = directly overhead (90 elevation), edge = horizon (0 elevation). Azimuth maps to angle. Satellites color-coded by constellation (GPS=blue, GLONASS=red, Galileo=orange, BeiDou=green). Symbol indicates if used in fix.

**Below sky plot**: Bar chart of CN0 (signal strength) per satellite, sorted by PRN. Fix info: type (2D/3D), HDOP, VDOP, number of satellites used.

```bash
git commit -m "feat: add GNSS sky plot detail view with constellation tracking"
```

---

### Task 30: Camera Analysis Detail View

**Files:**
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/camera/CameraAnalysisScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/camera/CameraAnalysisViewModel.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/camera/HistogramCanvas.kt`

**Layout**:
- Camera preview (CameraX PreviewView) taking most of the screen
- Crosshair overlay at center — shows RGB values of pixel at crosshair
- Bottom panel: RGB histogram (three overlapping line charts, red/green/blue channels)
- Toggle between color analysis mode and PPG (heart rate) mode

```bash
git commit -m "feat: add camera analysis detail view with histogram and PPG mode"
```

---

### Task 31: Aircraft Tracker Detail View

**Files:**
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/aviation/AircraftTrackerScreen.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/aviation/AircraftTrackerViewModel.kt`
- Create: `feature/detail/src/main/java/com/sysop/tricorder/feature/detail/aviation/AircraftList.kt`

**Two modes** (toggle):

1. **Table view**: Sortable list of aircraft with callsign, altitude, speed, distance from user, heading. Tap to highlight on map.

2. **AR view**: Camera preview with aircraft markers overlaid using device heading + elevation + aircraft position. Each marker shows callsign + altitude. Uses phone compass + accelerometer to determine look direction.

```bash
git commit -m "feat: add aircraft tracker detail view with table and AR modes"
```

---

## Phase 8: Session Recording

### Task 32: Session Recorder Service

**Files:**
- Create: `feature/session/build.gradle.kts`
- Create: `feature/session/src/main/java/com/sysop/tricorder/feature/session/SessionRecorder.kt`
- Create: `feature/session/src/main/java/com/sysop/tricorder/feature/session/RecordingService.kt`
- Create: `feature/session/src/test/java/com/sysop/tricorder/feature/session/SessionRecorderTest.kt`
- Modify: `settings.gradle.kts`

**SessionRecorder**: Core logic (not Android-specific). Takes `SensorRegistry` + `SessionDao`. Methods:
- `startRecording(name, location, activeProviders)` — creates Session, begins collecting readings
- `stopRecording()` — sets endTime, returns Session
- `isRecording: StateFlow<Boolean>`
- `currentSession: StateFlow<Session?>`
- `elapsed: StateFlow<Duration>`

Internally: launches a coroutineScope, merges all active provider flows, buffers readings (batch insert every 1 second for DB efficiency).

**RecordingService**: Foreground service with notification showing elapsed time. Keeps recording alive when app is backgrounded.

**Test**: Verify SessionRecorder with fake providers and in-memory database — start recording, emit readings, stop, verify readings persisted.

```bash
git commit -m "feat: add session recorder with foreground service"
```

---

### Task 33: Session List and Replay

**Files:**
- Create: `feature/session/src/main/java/com/sysop/tricorder/feature/session/list/SessionListScreen.kt`
- Create: `feature/session/src/main/java/com/sysop/tricorder/feature/session/list/SessionListViewModel.kt`
- Create: `feature/session/src/main/java/com/sysop/tricorder/feature/session/replay/SessionReplayScreen.kt`
- Create: `feature/session/src/main/java/com/sysop/tricorder/feature/session/replay/SessionReplayViewModel.kt`

**SessionListScreen**: LazyColumn of session cards. Each card shows: name, date/time, duration, location (small static map thumbnail), number of readings. Swipe to delete. Tap to open replay.

**SessionReplayScreen**: Map view showing recorded path as polyline. Playback controls (play/pause, speed 1x/2x/5x, scrub bar). As replay progresses, shows the sensor readings at each timestamp with the same overlays as live view. Timeline scrubber at bottom.

```bash
git commit -m "feat: add session list and replay screens"
```

---

### Task 34: Session Export

**Files:**
- Create: `feature/session/src/main/java/com/sysop/tricorder/feature/session/export/SessionExporter.kt`
- Create: `feature/session/src/test/java/com/sysop/tricorder/feature/session/export/SessionExporterTest.kt`

**SessionExporter**: Takes a Session + readings from DB. Exports to:

1. **CSV**: One file per provider. Columns: timestamp, lat, lon, [all value keys]. Written to app's Documents directory.
2. **JSON**: Complete session with all readings. One file.
3. **GPX**: GPS track with waypoints at readings. Sensor values as waypoint extensions.

Uses Android's `ShareSheet` to let user send the exported file.

**Test**: Verify CSV and JSON output format with known input data.

```bash
git commit -m "feat: add session export (CSV, JSON, GPX)"
```

---

## Phase 9: Settings & Polish

### Task 35: Settings Screen

**Files:**
- Create: `feature/settings/build.gradle.kts`
- Create: `feature/settings/src/main/java/com/sysop/tricorder/feature/settings/SettingsScreen.kt`
- Create: `feature/settings/src/main/java/com/sysop/tricorder/feature/settings/SettingsViewModel.kt`
- Modify: `settings.gradle.kts`

**Sections**:
- **API Keys**: Text fields for OpenWeatherMap, WAQI, N2YO, OpenCelliD keys. Show which are configured.
- **Sensor Settings**: Default sample rate slider. Toggle individual sensors on/off.
- **Session Settings**: Retention period (days). Auto-record on launch toggle.
- **Map Settings**: Map style selector. Offline tile download.
- **About**: Version, open source licenses, links.

```bash
git commit -m "feat: add settings screen with API key management"
```

---

### Task 36: Permission Handling

**Files:**
- Create: `app/src/main/java/com/sysop/tricorder/permission/PermissionManager.kt`
- Create: `app/src/main/java/com/sysop/tricorder/permission/PermissionScreen.kt`

**PermissionManager**: Checks and requests permissions grouped by sensor category. Shows which permissions are granted/denied and what sensors they unlock. Uses `rememberLauncherForActivityResult` with `RequestMultiplePermissions`.

**PermissionScreen**: Shown on first launch or when a sensor needs a permission. Grouped by category with explanations of why each permission is needed. "Grant All" button + individual toggles.

Runtime permission flow: when user taps a category tab that needs ungrated permissions, show a dialog explaining what's needed before requesting.

```bash
git commit -m "feat: add permission management with grouped requests"
```

---

### Task 37: Wire Everything Together in App Module

**Files:**
- Modify: `app/build.gradle.kts` (add all module dependencies)
- Create: `app/src/main/java/com/sysop/tricorder/di/DatabaseModule.kt`
- Modify: `app/src/main/java/com/sysop/tricorder/MainActivity.kt`

Add all module dependencies to `app/build.gradle.kts`:
```kotlin
implementation(project(":core:model"))
implementation(project(":core:sensor-api"))
implementation(project(":core:database"))
implementation(project(":core:network"))
implementation(project(":core:datastore"))
implementation(project(":core:ui-common"))
implementation(project(":feature:map"))
implementation(project(":feature:session"))
implementation(project(":feature:settings"))
implementation(project(":feature:detail"))
implementation(project(":sensor:motion"))
implementation(project(":sensor:environment"))
implementation(project(":sensor:location"))
implementation(project(":sensor:rf"))
implementation(project(":sensor:audio"))
implementation(project(":sensor:camera"))
implementation(project(":sensor:weather"))
implementation(project(":sensor:airquality"))
implementation(project(":sensor:aviation"))
implementation(project(":sensor:seismic"))
implementation(project(":sensor:radiation"))
implementation(project(":sensor:space"))
implementation(project(":sensor:rfintel"))
implementation(project(":sensor:tides"))
```

**DatabaseModule**: Provides Room database instance and DAOs.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TricorderDatabase =
        Room.databaseBuilder(context, TricorderDatabase::class.java, "tricorder.db")
            .build()

    @Provides
    fun provideSessionDao(db: TricorderDatabase): SessionDao = db.sessionDao()
}
```

**Step: Full build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

```bash
git add -A
git commit -m "feat: wire all modules together in app, add database DI"
```

---

### Task 38: End-to-End Smoke Test

**Files:**
- Create: `app/src/androidTest/java/com/sysop/tricorder/SmokeTest.kt`

Basic instrumented test that:
1. Launches `MainActivity`
2. Verifies the map screen appears
3. Verifies category tabs are visible
4. Taps a category tab and verifies overlay toggling works
5. Verifies navigation to a detail view works

Uses Compose testing APIs (`createAndroidComposeRule`).

Run: `./gradlew connectedAndroidTest`

```bash
git commit -m "test: add end-to-end smoke test"
```

---

## Summary

| Phase | Tasks | What's Built |
|-------|-------|-------------|
| 1: Scaffolding | 1 | Android project, Gradle multi-module |
| 2: Core | 2-3 | Domain models, SensorProvider interface, SensorRegistry |
| 3: Data Layer | 4-6 | Room DB, Retrofit, DataStore |
| 4: Hardware Sensors | 7-12 | Motion, Environment, Location, RF, Audio, Camera providers |
| 5: API Providers | 13-20 | Weather, AQI, Aviation, Seismic, Radiation, Space, RF Intel, Tides |
| 6: Map & UI | 21-23 | Theme, MapLibre map screen, navigation |
| 7: Detail Views | 24-31 | Compass, Spectrum, RF Scanner, Motion, Barometer, Sky Plot, Camera, Aircraft |
| 8: Sessions | 32-34 | Recording service, replay, export |
| 9: Polish | 35-38 | Settings, permissions, wiring, smoke test |

**Total: 38 tasks across 9 phases.**

The recommended implementation order follows the dependency chain: core abstractions first, then providers (which can be parallelized), then UI (which consumes providers), then cross-cutting features (sessions), then polish.
