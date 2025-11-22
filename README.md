# AppFrame

[![kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue?logo=kotlin&logoColor=white)](https://kotlinlang.org)
![Static Badge](https://img.shields.io/badge/Desktop-blue)
[![Maven Central](https://reposilite.kotlin.website/api/badge/latest/releases/ru/workinprogress/appframe-desktop?name=snapshots&color=40c14a&prefix=v)](https://reposilite.kotlin.website/#/releases/ru/workinprogress/appframe-desktop)

A customizable window frame library for [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
desktop applications. Provides a modern, native-looking window with title bar and standard window controls.

![Screenshot](/Screenshot.png?raw=true "Example App Screenshot")

## Features

* Custom title bar with draggable area
* Minimize / Maximize / Fullscreen controls
* Double-click to toggle maximize
* Fully theme-aware — works inside any MaterialTheme
* Undecorated window by default
* Drop-in replacement for your `Window` block

## Installation

Add the repository to your `settings.gradle.kts`

```kotlin
maven("https://reposilite.kotlin.website/releases")
```
And the dependency:
```kotlin
desktopMain.dependencies {
    implementation("ru.workinprogress:appframe-desktop:0.0.{version}")
}
```

#### Usage

```kotlin
fun main() =
    application {
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surfaceVariant = Color.Cyan)) {
            AppFrame(onCloseRequest = ::exitApplication) {
                App(modifier = Modifier.fillMaxSize())
            }
        }
    }
```
