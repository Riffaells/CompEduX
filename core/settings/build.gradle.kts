plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.settings)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

android {
    namespace = "com.riffaells.compedux.settings"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
