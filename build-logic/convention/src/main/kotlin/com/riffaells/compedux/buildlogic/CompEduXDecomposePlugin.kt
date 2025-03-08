package com.riffaells.compedux.buildlogic

import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CompEduXDecomposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<KotlinMultiplatformExtension> {
                // Настройка зависимостей для архитектуры приложения
                sourceSets.commonMain.dependencies {
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
                }
            }
        }
    }
}

// Extension function to make the code more readable
private fun DependencyHandler.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}
