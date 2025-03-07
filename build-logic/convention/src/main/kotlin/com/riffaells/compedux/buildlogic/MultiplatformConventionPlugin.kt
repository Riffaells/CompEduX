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
                val commonMain = sourceSets.maybeCreate("commonMain")
                val commonTest = sourceSets.maybeCreate("commonTest")

                commonMain.dependencies {
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.kotlin.stdlib)
                    implementation(libs.coroutines.core)
                    implementation(libs.napier)

                    // Decompose и Essenty
                    implementation(libs.decompose)
                    implementation(libs.decompose.compose)
                    implementation(libs.essenty.lifecycle)
                    implementation(libs.essenty.statekeeper)
                    implementation(libs.essenty.instancekeeper)
                    implementation(libs.essenty.backhandler)

                    // MVIKotlin
                    implementation(libs.mvikotlin)
                    implementation(libs.mvikotlin.main)
                    implementation(libs.mvikotlin.logging)
                    implementation(libs.mvikotlin.timetravel)
                    implementation(libs.mvikotlin.extensions.coroutines)

                    // Kodein DI
                    implementation(libs.kodein.di)


                }

                commonTest.dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }
}

// Extension function to make the code more readable
private fun DependencyHandler.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}
