plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.multiplatform.compose)
    alias(libs.plugins.compedux.decompose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Доступ к NetworkConfig интерфейсу из domain модуля
            implementation(projects.core.utils)
            implementation(projects.core.domain)
            implementation(projects.core.design)
            implementation(projects.feature.tree)
        }
    }
}
