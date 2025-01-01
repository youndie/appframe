# AppFrame

![Static Badge](https://img.shields.io/badge/Desktop-blue)

A lightweight AppFrame for [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
desktop targets.

![Screenshot](/Screenshot.png?raw=true "App Screenshot")

```kotlin
maven("https://reposilite.kotlin.website/releases")
```

```kotlin
desktopMain.dependencies {
	implementation("ru.workinprogress:appframe-desktop:0.0.1-snapshot")
}
```

#### Usage

```kotlin
@Composable
fun MyApplication() {
	AppFrame(
		onCloseRequest = { exitApplication() },
		title = "MyApp",
		appTheme = { content -> MyAppTheme { content() } },
		state = rememberWindowState(size = DpSize(1280.dp, 800.dp))
	) {
		Text("Welcome to My Application!")
	}
}
 ```

