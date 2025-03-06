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

// This makes the version catalog available to the convention plugins
// See: https://github.com/gradle/gradle/issues/15383
dependencies {
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "com.riffaells.compedux.multiplatform"
            implementationClass = "com.compedu.buildlogic.KotlinMultiplatformConventionPlugin"
        }
        register("kotlinMultiplatformLibrary") {
            id = "com.riffaells.compedux.multiplatform.library"
            implementationClass = "com.compedu.buildlogic.KotlinMultiplatformLibraryConventionPlugin"
        }
        register("kotlinMultiplatformCompose") {
            id = "com.riffaells.compedux.multiplatform.compose"
            implementationClass = "com.compedu.buildlogic.KotlinMultiplatformComposeConventionPlugin"
        }
        register("compEduXSettings") {
            id = "com.compedu.buildlogic.compEduXSettings"
            implementationClass = "com.compedu.buildlogic.CompEduXSettingsPlugin"
        }
    }
}
