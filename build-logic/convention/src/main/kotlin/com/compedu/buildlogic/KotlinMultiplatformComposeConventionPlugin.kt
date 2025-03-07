package com.compedu.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import libs

class KotlinMultiplatformComposeConventionPlugin : Plugin<Project> {
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
                val commonMain = sourceSets.maybeCreate("commonMain")
                val androidMain = sourceSets.maybeCreate("androidMain")
                val jvmMain = sourceSets.maybeCreate("jvmMain")
                val wasmJsMain = sourceSets.maybeCreate("wasmJsMain")

                // Configure Wasm target
                wasmJs {
                    browser()
                    binaries.executable()
                }

                // Configure common dependencies for all targets
                sourceSets.getByName("commonMain") {
                    dependencies {
                        // Access compose dependencies through the project's ComposeExtension
                        val composeE = project.extensions.getByType<ComposeExtension>()
                        val compose = composeE.dependencies
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material)
                        implementation(compose.ui)
                        @OptIn(ExperimentalComposeLibrary::class)
                        implementation(compose.components.resources)
                    }
                }

                // Wasm-specific dependencies if needed
                sourceSets.getByName("wasmJsMain") {
                    dependencies {
                        // Add Wasm-specific dependencies here if needed
                    }
                }
            }
        }
    }
}

// Extension function to make the code more readable
private fun DependencyHandler.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}
