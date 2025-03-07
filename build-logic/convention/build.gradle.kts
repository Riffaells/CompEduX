plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}



gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "com.riffaells.compedux.multiplatform"
            implementationClass = "com.riffaells.compedux.buildlogic.KotlinMultiplatformConventionPlugin"
        }
        register("kotlinMultiplatformLibrary") {
            id = "com.riffaells.compedux.multiplatform.library"
            implementationClass = "com.riffaells.compedux.buildlogic.KotlinMultiplatformLibraryConventionPlugin"
        }
        register("kotlinMultiplatformCompose") {
            id = "com.riffaells.compedux.multiplatform.compose"
            implementationClass = "com.riffaells.compedux.buildlogic.KotlinMultiplatformComposeConventionPlugin"
        }
        register("compEduXSettings") {
            id = "com.riffaells.compedux.multiplatform.settings"
            implementationClass = "com.riffaells.compedux.buildlogic.CompEduXSettingsPlugin"
        }
    }
}
