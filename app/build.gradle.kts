import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

compose {
    desktop {
        application {
            mainClass = "ru.workinprogress.appframe.MainKt"
        }
    }
}

kotlin {
    jvmToolchain(21)

    jvm("desktop") {
    }

    sourceSets {
        val desktopMain by getting

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(project(":appframe"))
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}
