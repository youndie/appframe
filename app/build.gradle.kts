import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("io.github.youndie.sborka.kmp")
    id("io.github.youndie.sborka.lint")
}

compose {
    desktop {
        application {
            mainClass = "io.github.youndie.appframe.MainKt"

            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "AppFrame"
                packageVersion = "1.0.0"
            }
        }
    }
}

kotlin {
    // OFF here and on in `:appframe`, which is the difference between the two modules: explicit
    // visibilities and return types are spelled out for a CONSUMER compiling against the artefact,
    // and this module is the demo — nobody depends on it, and it publishes nothing. The conventions
    // default it on because the library beside it is what they are shaped for.
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Disabled

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(projects.appframe)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}
