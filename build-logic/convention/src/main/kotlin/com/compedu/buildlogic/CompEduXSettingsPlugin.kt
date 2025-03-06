package com.compedu.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CompEduXSettingsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                "commonMainImplementation"(libs.findLibrary("multiplatform-settings").get())
                "commonMainImplementation"(libs.findLibrary("multiplatform-settings-coroutines").get())
                "commonMainImplementation"(libs.findLibrary("multiplatform-settings-serialization").get())
            }
        }
    }
}
