package com.riffaells.compedux.buildlogic

import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MultiplatformComposeConventionPlugin : Plugin<Project> {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {

                apply(libs.plugins.kmultiplatform.get().pluginId)
                apply(libs.plugins.kcompose.get().pluginId)
                apply(libs.plugins.compose.compiler.get().pluginId)
            }

            extensions.configure<ComposeExtension> {
                // Configure Compose-specific settings if needed
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // Configure source sets
                val commonMain = sourceSets.commonMain
                val wasmJsMain = sourceSets.wasmJsMain

                // Configure Wasm target
                wasmJs {
                    browser()
                    binaries.executable()
                }


                // Configure common dependencies for all targets
                commonMain {
                    dependencies {
                        // Access compose dependencies through the project's ComposeExtension
                        val composeE = project.extensions.getByType<ComposeExtension>()
                        val compose = composeE.dependencies
                        implementation(compose.ui)
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.materialIconsExtended)
                        implementation(compose.components.resources)
                        implementation(compose.components.uiToolingPreview)
                    }
                }

                // Wasm-specific dependencies if needed
                wasmJsMain {
                    dependencies {
                        // Add Wasm-specific dependencies here if needed
                    }
                }
            }
        }
    }
}
