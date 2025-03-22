plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.settings)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Доступ к NetworkConfig интерфейсу из domain модуля
            implementation(projects.core.domain)
        }
    }
}
