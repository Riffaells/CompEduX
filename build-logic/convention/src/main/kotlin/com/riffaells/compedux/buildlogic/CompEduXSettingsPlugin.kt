package com.riffaells.compedux.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import libs

class CompEduXSettingsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                "commonMainImplementation"(libs.multiplatform.settings)
                "commonMainImplementation"(libs.multiplatform.settings.coroutines)
                "commonMainImplementation"(libs.multiplatform.settings.serialization)
            }
        }
    }
}
