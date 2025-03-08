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

                commonMain.dependencies {
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.kotlin.stdlib)
                    implementation(libs.napier)

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

                }
            }
        }
    }
}

// Extension function to make the code more readable
private fun DependencyHandler.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}
