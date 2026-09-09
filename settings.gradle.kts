enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // The build conventions live here, not on the plugin portal. They have to be spelled out
        // by hand: `pluginManagement` is evaluated before any settings plugin is applied —
        // including the sborka one, which is fetched through it. The viddik plugin used to be the
        // other reason for this line and is not any more: 0.4.0 is on Maven Central, declared
        // above, and this server answers 404 for it.
        //
        // FILTERED, which it was not: an unfiltered repository takes part in resolving EVERY plugin,
        // so it is asked for coordinates it has never heard of, and the day its host is unreachable
        // Gradle disables it and fails plugins it never served.
        maven("https://reposilite.kotlin.website/snapshots") {
            name = "wip-snapshots"
            content {
                // One group, and it is the only one there can be. The portfolio's move to
                // `io.github.youndie` is finished: nothing this build resolves is under
                // `ru.workinprogress` any more, and a filter naming a group the server is never asked
                // about reads as a dependency that is still there.
                includeGroupByRegex("io\\.github\\.youndie.*")
            }
        }
    }
}

plugins {
    // Where dependencies are looked for: google() and mavenCentral() with their group filters, and
    // the snapshot repository the portfolio's own libraries are published to — filtered there too,
    // for the same reason as above. This file declared all three itself, the last one unfiltered.
    //
    // It also brings the check that this repository's `.editorconfig` is the one the rest of the
    // portfolio uses, which is the other half of pinning the formatter's version.
    id("io.github.youndie.sborka.settings") version "0.4.0.43"
}

// Must differ from the ":appframe" subproject name — typesafe project accessors clash otherwise.
rootProject.name = "appframe-root"

include(":app")
include(":appframe")
