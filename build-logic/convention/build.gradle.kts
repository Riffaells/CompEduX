plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)

    // Добавляем зависимость на BuildConfig плагин
    implementation(libs.buildConfig.plugin)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "com.riffaells.compedux.multiplatform"
            implementationClass = "com.riffaells.compedux.buildlogic.MultiplatformConventionPlugin"
        }
        register("kotlinMultiplatformLibrary") {
            id = "com.riffaells.compedux.multiplatform.library"
            implementationClass = "com.riffaells.compedux.buildlogic.MultiplatformLibraryConventionPlugin"
        }
        register("kotlinMultiplatformCompose") {
            id = "com.riffaells.compedux.multiplatform.compose"
            implementationClass = "com.riffaells.compedux.buildlogic.MultiplatformComposeConventionPlugin"
        }
        register("compEduXSettings") {
            id = "com.riffaells.compedux.multiplatform.settings"
            implementationClass = "com.riffaells.compedux.buildlogic.CompEduXSettingsPlugin"
        }
        register("compEduXConfig") {
            id = "com.riffaells.compedux.multiplatform.config"
            implementationClass = "com.riffaells.compedux.buildlogic.CompEduXConfigPlugin"
        }
        register("compEduXNetworking") {
            id = "com.riffaells.compedux.multiplatform.networking"
            implementationClass = "com.riffaells.compedux.buildlogic.CompEduXNetworkingPlugin"
        }
        register("compEduXDecompose") {
            id = "com.riffaells.compedux.multiplatform.decompose"
            implementationClass = "com.riffaells.compedux.buildlogic.CompEduXDecomposePlugin"
        }
    }
}
