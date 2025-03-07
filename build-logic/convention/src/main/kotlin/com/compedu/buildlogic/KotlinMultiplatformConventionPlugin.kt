package com.compedu.buildlogic

import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // Configure source sets
                val commonMain = sourceSets.maybeCreate("commonMain")
                val commonTest = sourceSets.maybeCreate("commonTest")

                commonMain.dependencies {
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.kotlin.stdlib)
                    implementation(libs.coroutines.core)
                    implementation(libs.kermit)
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
