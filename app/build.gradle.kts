import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.compose)
    alias(libs.plugins.compedux.decompose)
    alias(libs.plugins.android.application)
}

kotlin {

    jvmToolchain(libs.versions.jvm.get().toInt())
    androidTarget {
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }



    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }


    sourceSets {
        commonMain.dependencies {

            implementation(projects.core.utils)

            // Module dependencies
            implementation(projects.core.common)
            implementation(projects.feature.settings)
            implementation(projects.core.ui)
            implementation(projects.core.design)
        }

    }
}

android {
    namespace = "com.riffaells.compedux"
    compileSdk = 35

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        applicationId = "com.riffaells.compedux"

        versionCode = libs.versions.app.version.get().toInt()
        versionName = libs.versions.app.name.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Настройка тестов
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}




compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "CompEduX"
            packageVersion = libs.versions.app.name.get()

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "com.riffaells.compedux.desktopApp"
            }
        }
    }
}
