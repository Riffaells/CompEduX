// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Apply the plugins to the root project, but don't apply them
    alias(libs.plugins.kmultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kcompose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.hot) apply false
}

// Configure all projects
//allprojects {
//    group = "com.riffaells.compedux"
//    version = libs.versions.app.name.get()
//}
allprojects {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = libs.versions.jvm.get()
        targetCompatibility = libs.versions.jvm.get()
    }
}
