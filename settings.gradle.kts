enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // The viddik Gradle plugin lives here, not on the plugin portal, and so do the build
        // conventions. It has to be spelled out by hand: `pluginManagement` is evaluated before any
        // settings plugin is applied — including the sborka one, which is fetched through it.
        //
        // FILTERED, which it was not: an unfiltered repository takes part in resolving EVERY plugin,
        // so it is asked for coordinates it has never heard of, and the day its host is unreachable
        // Gradle disables it and fails plugins it never served.
        maven("https://reposilite.kotlin.website/snapshots") {
            name = "wip-snapshots"
            content { includeGroupByRegex("ru\\.workinprogress.*") }
        }
    }
}

plugins {
    // Where dependencies are looked for: google() and mavenCentral() with their group filters, and
    // the snapshot repository viddik is published to — filtered there too, for the same reason as
    // above. This file declared all three itself, the last one unfiltered.
    //
    // It also brings the check that this repository's `.editorconfig` is the one the rest of the
    // portfolio uses, which is the other half of pinning the formatter's version.
    id("ru.workinprogress.sborka.settings") version "0.1.0.18"
}

// Must differ from the ":appframe" subproject name — typesafe project accessors clash otherwise.
rootProject.name = "appframe-root"

include(":app")
include(":appframe")
