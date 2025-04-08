package com.riffaells.compedux.buildlogic

import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CompEduXNetworkingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<KotlinMultiplatformExtension> {
                // Настройка общих зависимостей для работы с сетью
                sourceSets.commonMain.dependencies {
                    // Ktor для работы с сетью
                    implementation(libs.ktor.client.core)
                    implementation(libs.ktor.client.content.negotiation)
                    implementation(libs.ktor.serialization.kotlinx.json)
                    implementation(libs.ktor.client.logging)
                    implementation(libs.ktor.client.auth)
                }

                // Платформо-специфичные зависимости
                sourceSets.androidMain.dependencies {
                    implementation(libs.ktor.client.android)
                }

                sourceSets.iosMain.dependencies {
                    implementation(libs.ktor.client.darwin)
                }

                sourceSets.jvmMain.dependencies {
                    implementation(libs.ktor.client.okhttp)
                }

                sourceSets.wasmJsMain.dependencies {
                    implementation(libs.ktor.client.js)
                }

                // Тестовые зависимости - только самое необходимое для API тестирования
                sourceSets.commonTest.dependencies {
                    implementation(libs.kotlinx.coroutines.test)
                    implementation(libs.ktor.client.mock)
                }
            }
        }
    }
}
