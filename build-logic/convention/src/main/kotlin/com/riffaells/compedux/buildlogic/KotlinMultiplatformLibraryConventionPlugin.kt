package com.riffaells.compedux.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import libs

class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                    apply(libs.plugins.android.library.get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget()
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
                val commonMain = sourceSets.maybeCreate("commonMain")

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
