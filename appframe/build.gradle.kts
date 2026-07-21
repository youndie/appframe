plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ksp)
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
            implementation(libs.viddik.annotations)
            implementation(libs.viddik.testing.core)
            // @PreviewParameter, shared with Compose tooling.
            implementation(libs.compose.ui.tooling.preview)
            runtimeOnly(libs.junit.jupiter.engine)
            runtimeOnly(libs.junit.platform.launcher)
        }

        // viddik's KSP processor generates the component registry and the screenshot tests here.
        desktopTest.kotlin.srcDir("build/generated/ksp/desktop/desktopTest/kotlin")
    }
}

dependencies {
    add("kspDesktopTest", libs.viddik.processor)
}

/**
 * Screenshot tests live in their own task, not in `desktopTest`.
 *
 * Goldens are recorded on the CI runner (see `.github/workflows/record-goldens.yml`): Skia renders
 * text with whatever fonts the host has, so a golden recorded on macOS never matches Linux. Keeping
 * them out of `check` means a dev machine still gets a green `./gradlew build`, while CI — where the
 * fonts match the recording — runs them via `-Pviddik.verify`.
 */
val screenshotTest =
    tasks.register<Test>("screenshotTest") {
        val testCompilation =
            kotlin.targets
                .getByName("desktop")
                .compilations
                .getByName("test")

        description = "Verifies the recorded screenshot goldens."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        // Without this the task happily runs against stale classes and reports a green build.
        dependsOn(tasks.named("desktopTestClasses"))
        testClassesDirs = testCompilation.output.classesDirs
        classpath = files(testCompilation.output.allOutputs, testCompilation.runtimeDependencyFiles)
        filter { includeTestsMatching("*GeneratedViddikTests*") }
        // The goldens are inputs; a re-recorded PNG has to re-run the verification.
        inputs
            .dir(layout.projectDirectory.dir("src/desktopTest/snapshots"))
            .withPropertyName("goldens")
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("viddik.snapshotsDir", "src/desktopTest/snapshots")
}

tasks.named<Test>("desktopTest") {
    // Owned by `screenshotTest` above; running them here too would just duplicate the work.
    filter { excludeTestsMatching("*GeneratedViddikTests*") }
}

if (providers.gradleProperty("viddik.verify").isPresent) {
    tasks.named("check") { dependsOn(screenshotTest) }
}
