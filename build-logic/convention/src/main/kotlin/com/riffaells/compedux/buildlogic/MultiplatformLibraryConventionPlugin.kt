package com.riffaells.compedux.buildlogic

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import libs

class MultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.library.get().pluginId)
            }

            // Настройка Android
            extensions.configure<LibraryExtension> {
                compileSdk = libs.versions.compileSdk.get().toInt()

                defaultConfig {
                    minSdk = libs.versions.minSdk.get().toInt()
                }

                // Установка совместимости Java для Android
                compileOptions {
                    sourceCompatibility = org.gradle.api.JavaVersion.toVersion(libs.versions.jvm.get())
                    targetCompatibility = org.gradle.api.JavaVersion.toVersion(libs.versions.jvm.get())
                }

                // Определяем namespace на основе имени проекта, если он не указан
                val projectPath = project.path.replace(":", ".")
                namespace = "com.riffaells.compedux${projectPath}"

                // Настройка исходных директорий
                sourceSets {
                    named("main") {
                        manifest.srcFile("src/androidMain/AndroidManifest.xml")
                        res.srcDirs("src/androidMain/res")
                    }
                }

                // Отключаем ненужные сборки для библиотек
                buildTypes {
                    release {
                        isMinifyEnabled = false
                    }
                }

            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {

                }

                jvm()

                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                }

                listOf(
                    iosX64(),
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach {
                    it.binaries.framework {
                        baseName = project.name
                        isStatic = true
                    }
                }

                // Configure source sets
                val commonMain = sourceSets.commonMain

                commonMain.dependencies {
                    // Common dependencies are handled by the base plugin
                }
            }


        }
    }
}

// Extension function to make the code more readable
private fun DependencyHandler.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}
