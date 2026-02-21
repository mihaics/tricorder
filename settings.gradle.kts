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
