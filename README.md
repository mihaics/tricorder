# Tricorder

A multi-sensor data aggregation and visualization app for Android, inspired by the Star Trek tricorder. Collects data from 13 sensor categories spanning device hardware sensors, RF radios, and external APIs, then displays everything on a unified map interface with dedicated detail views.

Built for Samsung Galaxy S24 Ultra (Android 15, API 35). Minimum SDK: 29.

## Features

### Device Hardware Sensors
- **Motion & Orientation** -- Accelerometer, gyroscope, magnetometer, rotation vector, step counter with real-time visualization
- **Environment** -- Barometric pressure, temperature, humidity, ambient light with altitude estimation and pressure trend analysis
- **Location & GNSS** -- GPS/GLONASS/Galileo/BeiDou satellite sky plot with signal strength (C/N0), satellite count, elevation/azimuth
- **RF Scanner** -- Bluetooth LE device scanning (RSSI, TX power), WiFi network scanning (SSID, BSSID, channel, signal), cellular tower info
- **Audio Spectrum** -- Real-time FFT spectrogram, dB SPL meter, peak frequency detection
- **Camera Analysis** -- Live CameraX preview with color analysis (RGB channels), brightness measurement, front/back camera switching

### External Data APIs
- **Weather** -- Temperature, humidity, wind speed/direction, UV index, atmospheric pressure via [Open-Meteo](https://open-meteo.com/) (free, no key)
- **Air Quality** -- AQI index with PM2.5, PM10, O3, NO2, SO2, CO pollutant breakdown via [WAQI](https://aqicn.org/api/) (key required)
- **Aircraft Tracker** -- Live aircraft positions with callsign, altitude, velocity, heading via [OpenSky Network](https://opensky-network.org/) (free, no key). Map view with directional markers
- **Earthquakes** -- Recent seismic events (M2.5+) within 500km over the last 7 days via [USGS](https://earthquake.usgs.gov/fdsnws/event/1/) (free, no key)
- **Radiation** -- Background radiation levels (CPM, uSv/h) via [Safecast](https://api.safecast.org/) (free, no key)
- **Satellites** -- Orbital satellite tracking via [N2YO](https://www.n2yo.com/api/) (key required)
- **Tides** -- Water level predictions from NOAA stations via [NOAA CO-OPS](https://tidesandcurrents.noaa.gov/api/) (free, no key)
- **Cell Tower Database** -- Tower locations and coverage via [OpenCelliD](https://opencellid.org/) (key required)

All external data is correlated with the device's GPS location in real-time.

### Session Recording
- Record sensor data from all active providers simultaneously
- Background recording via foreground service
- Buffered writes to Room database (flush every 1 second)
- Export sessions to CSV, JSON, or GPX formats
- Session replay with timeline scrubbing
- Configurable retention period (default 30 days)

## Architecture

24-module Gradle project following a clean architecture pattern:

```
:app                        Single-activity entry point, navigation, permissions, DI

:core:model                 SensorReading, SensorCategory (13 types), Session
:core:sensor-api            SensorProvider interface, SensorRegistry, DeviceLocation
:core:database              Room database (sessions + readings)
:core:network               OkHttp + Moshi singletons
:core:datastore             DataStore preferences (API keys, settings)
:core:ui-common             Dark theme, monospace typography, category colors

:feature:map                MapLibre map + resizable sensor panel
:feature:detail             11 detail screens with dedicated visualizations
:feature:session            Recording engine, export (CSV/JSON/GPX), replay
:feature:settings           API key management, sample rate, retention

:sensor:motion              Accelerometer, gyroscope, magnetometer, rotation, steps
:sensor:environment         Barometer, temperature, humidity, light
:sensor:location            Fused Location + GNSS satellite status
:sensor:rf                  BLE scan, WiFi scan, cellular info
:sensor:audio               AudioRecord + FFT
:sensor:camera              CameraX + color analysis
:sensor:weather             Open-Meteo API
:sensor:airquality          WAQI API
:sensor:aviation            OpenSky Network API
:sensor:seismic             USGS Earthquake API
:sensor:radiation           Safecast API
:sensor:space               N2YO API
:sensor:rfintel             OpenCelliD API
:sensor:tides               NOAA CO-OPS API
```

### Sensor Provider System

All sensors implement the `SensorProvider` interface and are auto-discovered via Hilt `@Binds @IntoSet` multibinding into `SensorRegistry`. Each provider exposes:
- `id` / `name` / `category` -- identification
- `availability()` -- hardware check, permission check, or API key requirement
- `readings(): Flow<SensorReading>` -- reactive stream of sensor data
- `mapOverlay()` -- how to visualize on the map (heatmap, markers, circles, etc.)

### Key Design Decisions
- **Flow throttling** -- High-frequency sensors (motion at ~200Hz, BLE scans) are throttled with `sample(250ms)` and `ConcurrentHashMap` to prevent ANR
- **Thread safety** -- Blocking operations (`AudioRecord.read`, network calls) use `flowOn(Dispatchers.IO)`, sensor collection uses `flowOn(Dispatchers.Default)`
- **Compass smoothing** -- Low-pass filter with circular averaging (alpha=0.85) prevents jitter at the 0/360 degree boundary
- **Adaptive normalization** -- Audio spectrogram uses per-frame normalization instead of fixed divisor
- **Passive location** -- `FusedLocationProviderClient` runs in `TricorderApp` with balanced power priority (30s interval) to keep `DeviceLocation` updated for all external API providers

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose (Material 3, BOM 2024.12.01) |
| DI | Hilt / Dagger 2.53.1 |
| Async | Kotlin Coroutines 1.9.0 |
| Navigation | Navigation Compose 2.8.5 |
| Map | MapLibre Android SDK 11.8.2 (OpenFreeMap tiles) |
| Camera | CameraX 1.4.1 |
| Database | Room 2.6.1 |
| Preferences | DataStore 1.1.1 |
| Networking | Retrofit 2.11.0 + Moshi 1.15.1 + OkHttp 4.12.0 |
| Charts | Vico 2.0.0-beta.3 |
| Location | Play Services Location 21.3.0 |
| Build | AGP 8.7.3, KSP 2.1.0 |
| Testing | JUnit 4, Google Truth, Turbine, MockK |

## Building

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

Requires:
- JDK 17
- Android SDK 35
- Gradle 8.7+ (included via wrapper)

## Permissions

The app requests the following permissions at startup, grouped by sensor category:

| Permission | Used By |
|---|---|
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Location, all external APIs |
| `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` | BLE scanner |
| `NEARBY_WIFI_DEVICES` | WiFi scanner |
| `CAMERA` | Camera analysis, aircraft tracker map |
| `RECORD_AUDIO` | Audio spectrum analyzer |
| `BODY_SENSORS` | Motion sensors |
| `ACTIVITY_RECOGNITION` | Step counter |

Network permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`) are granted automatically.

## Configuration

API keys for premium data sources can be configured in Settings:

| Service | Key | Free Tier |
|---|---|---|
| WAQI (Air Quality) | Required | Yes, with "demo" token |
| N2YO (Satellites) | Required | Yes, limited requests |
| OpenCelliD (Cell Towers) | Required | Yes, with registration |
| OpenWeatherMap | Optional | Not currently used |

All other data sources (Open-Meteo, OpenSky, USGS, Safecast, NOAA) are free and require no API keys.

## UI Overview

The main screen is a split layout with a MapLibre map on top and a sensor panel on the bottom. The panel is user-resizable via a drag handle (15%-75% of screen height).

**Category selector** -- Horizontal scrollable row of 13 color-coded category icons. Tap to activate/deactivate sensor streams. Tap readings to open detail views.

**Detail screens** with dedicated visualizations:
- Motion -- real-time accelerometer/gyroscope/magnetometer values
- Barometer -- pressure, altitude, light, pressure trend analysis
- GNSS Sky Plot -- satellite positions on a polar plot with signal strength
- RF Scanner -- BLE/WiFi/cellular device lists with signal levels
- Audio Spectrum -- FFT spectrogram with dB meter and peak frequency
- Camera -- live preview with RGB analysis and brightness
- Weather -- temperature, humidity, wind, UV index, pressure cards
- Air Quality -- AQI gauge with pollutant breakdown
- Aircraft Tracker -- table view + map view with directional markers
- Earthquakes -- magnitude-sorted list with color-coded severity
- Compass -- compass rose with magnetic field strength

## Default Location

When GPS is not yet available, the app defaults to Arad, Romania (46.1866, 21.3123). This is updated automatically once the device obtains a GPS fix.

## License

MIT License. See [LICENSE](LICENSE) for details.
