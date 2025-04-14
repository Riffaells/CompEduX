import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.ComposeHotRun
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.compose)
    alias(libs.plugins.compedux.decompose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.hot)
}

composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvm.get().toInt())
        vendor.set(JvmVendorSpec.JETBRAINS)
    }
}

tasks.register<ComposeHotRun>("runHot") {
    mainClass.set("DevMainKt")
}

tasks.named("runHot") {
    this as ComposeHotRun
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get().toInt()))
        vendor.set(JvmVendorSpec.JETBRAINS)
    })
}

// Решаем проблему с перезаписью MANIFEST.MF
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Также настраиваем процессирование ресурсов
tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

kotlin {
    // Регистрируем все необходимые таргеты явно
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    // Подключаем JVM таргет для десктоп
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
    compileSdk = libs.versions.compileSdk.get().toInt()

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
