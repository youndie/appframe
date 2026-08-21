plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ksp)
    alias(libs.plugins.viddik)
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

mavenPublishing {
    coordinates(
        groupId = "ru.workinprogress",
        artifactId = "appframe",
        version = "0.0.${providers.gradleProperty("BUILD_NUMBER").getOrElse("1-snapshot")}",
    )

    pom {
        name.set("AppFrame")
        description.set("Platform-aware window frame for Compose Multiplatform desktop applications")
        url.set("https://github.com/youndie/appframe")

        developers {
            developer {
                id.set("youndie")
                name.set("Pavel Votyakov")
                email.set("panic.xyb@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/youndie/appframe")
        }
    }
}

kotlin {
    jvmToolchain(21)
    explicitApi()

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
            implementation(libs.kotlin.test)
            // `desktopMain` deliberately depends on `common`; rendering a real window in tests needs
            // the host's skiko native library, which only `currentOs` brings in.
            implementation(compose.desktop.currentOs)
            // @PreviewParameter, shared with Compose tooling.
            implementation(libs.compose.ui.tooling.preview)
            // The viddik artifacts, its KSP processor, the JUnit 5 runtime and the generated-source
            // directory all come from the `ru.workinprogress.viddik` plugin.
        }
    }
}

/**
 * Screenshot tests run as part of `check`.
 *
 * They used to be kept out of it: the goldens were host-specific, so they had to be recorded on the
 * CI runner that verified them and a dev machine could not have gone green. Since the fixtures draw
 * in viddik's bundled font (`viddikTypography()` in `TitleBarScreenshots.kt`) the goldens are the
 * same everywhere, so `./gradlew build` verifies them wherever it runs.
 *
 * `snapshotsDir` defaults to src/desktopTest/snapshots, which is where the goldens are.
 */
viddik {
    verifyOnCheck = true
}

tasks.withType<Test>().configureEach {
    // HostOsTest is kotlin.test on JUnit 5, same as the generated screenshot tests.
    useJUnitPlatform()
}
