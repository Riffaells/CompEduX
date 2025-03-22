plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.decompose)
}

kotlin {

    sourceSets {
        commonMain.dependencies {


            implementation(projects.feature.settings)
            implementation(projects.core.domain)

        }
    }
}