plugins {
    alias(wip.plugins.kotlinMultiplatform)
    alias(wip.plugins.composeCompiler)
    alias(wip.plugins.composeMultiplatform)
    alias(wip.plugins.ksp)
    alias(libs.plugins.viddik)
    id("io.github.youndie.sborka.kmp")
    id("io.github.youndie.sborka.lint")
    // The pom, the sources jar and — because `sborka.central` is on — the javadoc jar and the
    // signatures Maven Central refuses a bundle without. The coordinate is `sborka.group`, the module
    // name and the `version` property; the release goes out through sborka's `central.yaml`.
    id("io.github.youndie.sborka.publish")
}

publishing {
    repositories {
        maven {
            name = "kotlinWebsite"
            url = uri("https://reposilite.kotlin.website/releases")

            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting

        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(compose.material3)
        }
        desktopMain.dependencies {
            // `common`, not `currentOs`: a published POM must not pin a host-specific skiko artifact.
            api(compose.desktop.common)
        }
        desktopTest.dependencies {
            implementation(wip.kotlin.test)
            // `desktopMain` deliberately depends on `common`; rendering a real window in tests needs
            // the host's skiko native library, which only `currentOs` brings in.
            implementation(compose.desktop.currentOs)
            // @PreviewParameter, shared with Compose tooling.
            implementation(wip.compose.ui.tooling.preview)
            // The viddik artifacts, its KSP processor, the JUnit 5 runtime and the generated-source
            // directory all come from the `io.github.youndie.viddik` plugin.
        }
    }
}

// Screenshot tests run as part of `check`.
//
// They used to be kept out of it: the goldens were host-specific, so they had to be recorded on the
// CI runner that verified them and a dev machine could not have gone green. Since the fixtures draw
// in viddik's bundled font (`viddikTypography()` in `TitleBarScreenshots.kt`) the goldens are the
// same everywhere, so `./gradlew build` verifies them wherever it runs.
//
// `snapshotsDir` defaults to src/desktopTest/snapshots, which is where the goldens are.
viddik {
    verifyOnCheck = true
}

tasks.withType<Test>().configureEach {
    // HostOsTest is kotlin.test on JUnit 5, same as the generated screenshot tests.
    useJUnitPlatform()
}
