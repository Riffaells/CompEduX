# Compose Multiplatform Application

## Before running!
 - check your system with [KDoctor](https://github.com/Kotlin/kdoctor)
 - install JDK 17 or higher on your machine
 - add `local.properties` file to the project root and set a path to Android SDK there

### Android
To run the application on android device/emulator:  
 - open project in Android Studio and run imported android run configuration

To build the application bundle:
 - run `./gradlew :app:assembleDebug`
 - find `.apk` file in `app/build/outputs/apk/debug/app-debug.apk`
Run android UI tests on the connected device: `./gradlew :app:connectedDebugAndroidTest`

### Desktop
Run the desktop application: `./gradlew :app:run`
Run desktop UI tests: `./gradlew :app:jvmTest`

### iOS
To run the application on iPhone device/simulator:
 - Open `iosApp/iosApp.xcproject` in Xcode and run standard configuration
 - Or use [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) for Android Studio
Run iOS simulator UI tests: `./gradlew :app:iosSimulatorArm64Test`

### Wasm Browser (Alpha)
Run the browser application: `./gradlew :app:wasmJsBrowserDevelopmentRun --continue`
Run browser UI tests: `./gradlew :app:wasmJsBrowserTest`

