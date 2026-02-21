# Tricorder App Design

## Overview

A hobbyist/maker Android app that turns a high-end phone (S24 Ultra) into a multi-sensor scanning device. Map-centric UI with sensor data overlaid geographically. Built with Kotlin + Jetpack Compose.

## Architecture: Modular Sensor Engine

Three-layer architecture:

1. **Sensor Abstraction Layer** - Unified `SensorProvider` interface for all data sources (hardware sensors, APIs, future external hardware). Each provider emits `SensorReading` objects via Kotlin Flows.
2. **Map Rendering Layer** - MapLibre with composable overlay system. Each sensor type registers its own map overlay renderer.
3. **Session Manager** - Recording/replay using Room DB for persistence and export.

### Layer Diagram

```
UI Layer (Compose)
  - Map View (MapLibre + Overlays)
  - Sensor Detail Sheets
  - Session Manager UI

ViewModel Layer
  - SensorDashboardViewModel
  - activeProviders, readings (Flow), overlays (Flow)

Sensor Engine (Domain)
  - SensorRegistry
  - SessionRecorder
  - OverlayRendererRegistry
  - SensorProvider interface

Provider Implementations
  - Hardware Sensors, API Clients, RF Scanner, Audio Analyzer

Data Layer
  - Room DB (sessions), Retrofit (APIs), DataStore (preferences)
```

### Key Design Decisions

- **SensorProvider is the core abstraction** - every data source implements the same interface
- **Each provider declares its own map overlay** via `MapOverlayConfig`
- **Kotlin Flows throughout** - natural backpressure, lifecycle-aware
- **Hilt DI** with multibindings for auto-discovery of providers
- **Session recording is cross-cutting** - subscribes to all active provider flows
- **Extensible for future external hardware** - BLE/USB sensors can implement SensorProvider without refactoring

## Sensor Inventory

### Built-in Hardware Sensors

| Category | Sensors | Data |
|----------|---------|------|
| Motion & Orientation | Accelerometer, Gyroscope, Magnetometer, Gravity, Rotation Vector, Step Counter, Significant Motion | 3-axis acceleration, angular velocity, compass heading, step count |
| Environmental | Barometer, Ambient Light, Proximity | Atmospheric pressure (altitude), lux, proximity distance |
| Location | GPS, GLONASS, Galileo, BeiDou | Lat/lng, altitude, speed, bearing, accuracy, satellite info |
| Imaging | Camera (200MP main, ultrawide, 2x tele, 5x tele) | Photo/video, color analysis, distance estimation |
| RF & Connectivity | WiFi, Bluetooth (BLE), Cellular, NFC, UWB | RSSI, SSID, BSSID, cell ID, MNC/MCC, NFC tags, UWB ranging |
| Audio | Microphone | Sound pressure level (dB), frequency spectrum (FFT) |
| Biometric | Heart rate (camera PPG) | BPM estimate |

### External API Data Sources

| Category | APIs | Data |
|----------|------|------|
| Weather | OpenWeatherMap, Open-Meteo | Temperature, humidity, wind, forecast, UV index |
| Air Quality | OpenAQ, WAQI | PM2.5, PM10, O3, NO2, SO2, CO, AQI |
| RF Intelligence | OpenCelliD, WiGLE | Cell tower locations, WiFi network database |
| Aviation | OpenSky Network, ADS-B Exchange | Aircraft positions, altitude, callsign |
| Space | N2YO, CelesTrak (TLE) | Satellite passes, ISS tracking |
| Seismic | USGS Earthquake API | Recent earthquakes, magnitude, depth |
| Radiation | Safecast API | Background radiation levels |
| Tides & Water | NOAA Tides & Currents | Tide predictions, water levels |

## UI Design

### Main Screen: Map-Centric

- **Full-screen map** as primary canvas with sensor data overlaid
- **Bottom category tabs** to toggle overlay visibility (multiple active simultaneously)
- **Quick readout bar** - horizontally scrollable numeric values from active sensors
- **Bottom sheet** - pull up for detailed sensor view with live graphs
- **Record button** - top-right toggle for session recording

### Map Overlay Types

| Sensor Category | Overlay Style |
|----------------|---------------|
| Weather/Air Quality | Color-coded heatmap around current location |
| WiFi Networks | Circular markers sized by signal strength |
| Cell Towers | Tower markers with coverage circles |
| Bluetooth Devices | Dots at estimated distance via RSSI |
| Aircraft | Plane icons with altitude labels |
| Satellites | Sky plot overlay (polar projection) |
| Earthquakes | Circles sized by magnitude, colored by recency |
| Sound | Directional gradient showing dB levels |
| Magnetic Field | Vector field arrows showing anomalies |

### Full-Screen Detail Views

Each sensor category has a purpose-built instrument view accessible by tapping through from the map:

| View | Content |
|------|---------|
| Compass / Magnetometer | Compass rose, magnetic/true north, field strength, tilt-compensated |
| Audio Spectrum | Real-time FFT spectrogram, waveform, dB meter, frequency cursor |
| RF Scanner | WiFi/BLE radar view, signal graphs, channel utilization |
| Motion / IMU | 3D orientation, accelerometer/gyro graphs, step counter |
| Barometer / Altimeter | Pressure trend graph, altitude, weather prediction |
| GNSS Sky Plot | Polar satellite plot (GPS/GLONASS/Galileo/BeiDou), CNR bars |
| Camera Analysis | Viewfinder with color histogram, RGB values, spectral analysis |
| Aircraft Tracker | AR view (camera + aircraft overlay) or table view |

## Session Recording

### Session Model

```
Session
  id: UUID
  name: String
  startTime: Instant
  endTime: Instant?
  location: LatLng
  activeProviders: List<String>
  readings: List<TimestampedReading>
    timestamp: Instant
    providerId: String
    values: Map<String, Any>
    location: LatLng?
```

### Features

- Start/stop from top bar, persists across backgrounding
- Replay at 1x/2x/5x speed on map with path and readings
- Export: CSV (per sensor), JSON (complete), KML/GPX (geotagged)
- Session list with date sorting and map thumbnail previews
- Configurable retention (auto-delete or keep forever)
- ~1MB per 10 minutes with all sensors at default sample rates

## Project Structure

```
tricorder/
  app/                        # App module, DI, navigation
  core/
    model/                    # SensorReading, Session, SensorCategory
    sensor-api/               # SensorProvider interface, SensorRegistry
    database/                 # Room DB, DAOs, entities
    network/                  # Retrofit setup, API base clients
    ui-common/                # Theme, shared composables, map utilities
  feature/
    map/                      # Main map screen, overlay rendering
    session/                  # Recording, replay, export
    settings/                 # Preferences, API key management
  sensor/
    motion/                   # Accelerometer, gyro, magnetometer
    environment/              # Barometer, light, proximity
    location/                 # GNSS, satellite info
    rf/                       # WiFi, BLE, cellular, NFC, UWB
    audio/                    # Microphone, FFT analysis
    camera/                   # Camera analysis
    weather/                  # OpenWeatherMap, Open-Meteo
    airquality/               # OpenAQ, WAQI
    aviation/                 # OpenSky, ADS-B
    seismic/                  # USGS earthquake
    radiation/                # Safecast
    space/                    # N2YO, CelesTrak
```

## Dependencies

| Dependency | Purpose |
|-----------|---------|
| MapLibre SDK | Open-source map rendering, offline tiles |
| Jetpack Compose + Material 3 | UI framework |
| Hilt | DI, sensor provider multibinding |
| Room | Local database for sessions |
| Retrofit + Moshi | HTTP clients for external APIs |
| Kotlin Coroutines + Flow | Async sensor data streams |
| Vico | Compose-native charting |
| DataStore | Preferences storage |
| CameraX | Camera access and analysis |
| TarsosDSP | Audio FFT / frequency analysis |

## Permissions

ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION, BLUETOOTH_SCAN, BLUETOOTH_CONNECT, NEARBY_WIFI_DEVICES, CAMERA, RECORD_AUDIO, BODY_SENSORS, ACTIVITY_RECOGNITION, NFC, INTERNET, ACCESS_NETWORK_STATE
