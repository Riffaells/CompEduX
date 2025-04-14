package com.riffaells.compedux.buildlogic

import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply(libs.plugins.kmultiplatform.get().pluginId)
                apply(libs.plugins.kotlin.serialization.get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // Configure source sets
                val commonMain = sourceSets.commonMain
                val commonTest = sourceSets.commonTest
                val androidMain = sourceSets.androidMain
                val jvmMain = sourceSets.jvmMain
                val wasmJsMain = sourceSets.wasmJsMain

                commonMain.dependencies {
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.kotlinx.datetime)
                    implementation(libs.kotlin.stdlib)
                    implementation(libs.napier)
//                    implementation(libs.slf4j.api)

                    // Kodein DI
                    implementation(libs.kodein)
                    implementation(libs.kodein.compose)

                    implementation(libs.coroutines)
                }

                commonTest.dependencies {
                    implementation(kotlin("test"))
                }

                androidMain.dependencies {
                    implementation(libs.coroutines.android)
                    implementation(libs.androidx.core)

                    // Добавляем SLF4J для Android
//                    implementation(libs.slf4j.android)
                }

                // JVM-специфические зависимости
                jvmMain.dependencies {
                    // Добавляем SLF4J для JVM (Desktop)
//                    implementation(libs.slf4j.simple)
                    implementation(libs.coroutines.swing)
                }

                // WASM-специфические зависимости
                wasmJsMain.dependencies {
                    // Для WASM просто используем Napier, т.к. SLF4J не нужен
                }
            }
        }
    }
}
