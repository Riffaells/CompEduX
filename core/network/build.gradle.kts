plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.networking)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.utils)
            implementation(projects.core.domain)
        }
    }
}
