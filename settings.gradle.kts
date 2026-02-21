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
include(":core:model")
include(":core:sensor-api")
include(":core:database")
include(":core:network")
include(":core:datastore")
include(":core:ui-common")
include(":feature:map")
include(":feature:detail")
include(":feature:session")
include(":sensor:motion")
include(":sensor:environment")
include(":sensor:location")
include(":sensor:rf")
include(":sensor:audio")
include(":sensor:camera")
include(":sensor:weather")
include(":sensor:airquality")
include(":sensor:aviation")
include(":sensor:seismic")
include(":sensor:radiation")
include(":sensor:space")
include(":sensor:rfintel")
include(":sensor:tides")
