package com.riffaells.compedux.buildlogic

import com.github.gmazzo.buildconfig.BuildConfigExtension
import libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.util.*

class CompEduXConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.buildConfig.get().pluginId)
            }

            // Настраиваем BuildConfig после применения плагина
            extensions.configure<BuildConfigExtension> {
                // Используем свойства из libs.versions.toml
                val appName = libs.versions.app.name.get().toString()
                val appVersion = libs.versions.app.version.get().toString()
                val appProject = libs.versions.app.project.get().toString()
                val appPackage = libs.versions.app.`package`.get().toString()

                packageName.set(appPackage)

                // Основные константы приложения
                buildConfigField("String", "APP_NAME", "\"$appName\"")
                buildConfigField("int", "APP_VERSION", appVersion)
                buildConfigField("String", "APP_PROJECT", "\"$appProject\"")
                buildConfigField("String", "APP_PACKAGE", "\"$appPackage\"")

                // Платформенные константы - определяются в Platform.kt runtime
                buildConfigField("String", "PLATFORM_TYPE", "\"Multi\"")
                buildConfigField("String", "BUILD_DATE", "\"${Date()}\"")

                // Дополнительные полезные константы
                buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")

                // Динамический флаг DEBUG, определяемый на основе задачи
                val isDebug = providers.provider {
                    project.gradle.startParameter.taskNames.any {
                        it.contains("debug", ignoreCase = true)
                    }
                }
                buildConfigField("boolean", "DEBUG", "${isDebug.get()}")


                useKotlinOutput {
                    internalVisibility = false
                }
            }
            extensions.configure<KotlinMultiplatformExtension> {
                // Configure source sets
                val commonMain = sourceSets.commonMain
                val commonTest = sourceSets.commonTest
                val wasmJsMain = sourceSets.wasmJsMain
                val desktopMain = sourceSets.jvmMain
                val androidMain = sourceSets.androidMain


            }

        }
    }
}
