plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.mavenPublish)
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

    jvm("desktop") {
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
