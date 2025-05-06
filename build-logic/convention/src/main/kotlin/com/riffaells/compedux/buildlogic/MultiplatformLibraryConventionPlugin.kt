package com.riffaells.compedux.buildlogic

import com.android.build.gradle.LibraryExtension
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.library.get().pluginId)
            }



            extensions.configure<KotlinMultiplatformExtension> {
//                jvmToolchain(libs.versions.jvm.get().toInt())
                androidTarget {

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


        }
    }
}
