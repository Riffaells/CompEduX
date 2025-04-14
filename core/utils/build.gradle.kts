import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
//    alias(libs.plugins.compedux.multiplatform.compose)
//    alias(libs.plugins.compedux.decompose)

//    alias(libs.plugins.compedux.multiplatform.compose)
    alias(libs.plugins.compedux.kconfig)
}

