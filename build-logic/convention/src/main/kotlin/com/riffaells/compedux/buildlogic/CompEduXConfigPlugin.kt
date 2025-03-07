package com.riffaells.compedux.buildlogic


import libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class CompEduXConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
//                apply(libs.plugins.kotlinJvm.get().pluginId)
                apply(libs.plugins.buildConfig.get().pluginId)


            }
        }
    }
}

