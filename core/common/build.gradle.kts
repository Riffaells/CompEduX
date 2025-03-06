plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    androidTarget()
    jvm()

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // Зависимости уже добавлены через convention plugin
        }
    }
}

android {
    namespace = "com.riffaells.compedux.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
