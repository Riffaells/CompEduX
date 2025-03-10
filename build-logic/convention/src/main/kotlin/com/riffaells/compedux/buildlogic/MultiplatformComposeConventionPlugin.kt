package com.riffaells.compedux.buildlogic

import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MultiplatformComposeConventionPlugin : Plugin<Project> {
    @OptIn(ExperimentalWasmDsl::class, ExperimentalComposeLibrary::class)
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
                // Set JVM target compatibility to match Java
                jvmToolchain(libs.versions.jvm.get().toInt())

                // Add opt-in annotations for experimental APIs
                targets.configureEach {
                    compilations.configureEach {
                        compileTaskProvider.configure {
                            compilerOptions {
                                // Add opt-in for Material3 experimental APIs
                                freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
                                // Add other common opt-ins if needed
                                freeCompilerArgs.add("-opt-in=androidx.compose.foundation.ExperimentalFoundationApi")
                                freeCompilerArgs.add("-opt-in=androidx.compose.animation.ExperimentalAnimationApi")
                                freeCompilerArgs.add("-opt-in=androidx.compose.ui.ExperimentalComposeUiApi")
                            }
                        }
                    }
                }

                // Configure source sets
                val commonMain = sourceSets.commonMain
                val commonTest = sourceSets.commonTest
                val wasmJsMain = sourceSets.wasmJsMain
                val desktopMain = sourceSets.jvmMain
                val androidMain = sourceSets.androidMain

                // Configure Wasm target
                wasmJs {
                    browser()
                    binaries.executable()
                }

                val composeE = project.extensions.getByType<ComposeExtension>()
                val compose = composeE.dependencies

                // Configure common dependencies for all targets
                commonMain {
                    dependencies {
                        // Access compose dependencies through the project's ComposeExtension
                        implementation(compose.ui)
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.materialIconsExtended)
                        implementation(compose.components.resources)
                        implementation(compose.components.uiToolingPreview)
                        implementation(compose.material3AdaptiveNavigationSuite)

                        implementation(libs.skiko)
                    }
                }

                commonTest {
                    dependencies {

                        implementation(kotlin("test"))
                        implementation(compose.uiTest)
                    }
                }

                desktopMain {
                    dependencies {
                        implementation(compose.desktop.currentOs)

                    }
                }
                androidMain {
                    dependencies {
                        implementation(compose.uiTooling)
                        implementation(libs.androidx.activityCompose)
                    }

                }

                // Wasm-specific dependencies if needed
                wasmJsMain {
                    dependencies {
                    }
                }
            }
        }
    }
}
