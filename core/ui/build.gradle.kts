plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.multiplatform.compose)
    alias(libs.plugins.compedux.decompose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Зависимость от common модуля для доступа к компонентам
            implementation(projects.core.design)
            implementation(projects.core.common)
            implementation(projects.feature.settings)
            implementation(projects.feature.tree)
        }
    }
}
