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
    // Core modules
    implementation(project(":core:model"))
    implementation(project(":core:sensor-api"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:ui-common"))

    // Feature modules
    implementation(project(":feature:map"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:settings"))

    // Sensor modules
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

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling.preview)
    implementation(libs.compose.icons.extended)
    debugImplementation(libs.compose.tooling)

    // Activity
    implementation(libs.activity.compose)

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
