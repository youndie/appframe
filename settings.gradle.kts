enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // The viddik Gradle plugin lives here, not on the plugin portal.
        maven("https://reposilite.kotlin.website/snapshots")
    }
}

dependencyResolutionManagement {
    repositories {
        // Compose Multiplatform pulls androidx artifacts that are only published to Google's repo.
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // Screenshot testing toolkit — https://github.com/youndie/viddik
        maven("https://reposilite.kotlin.website/snapshots")
    }
}

// Must differ from the ":appframe" subproject name — typesafe project accessors clash otherwise.
rootProject.name = "appframe-root"

include(":app")
include(":appframe")
