plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.decompose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Настройки
            implementation(projects.feature.settings)
            implementation(projects.feature.tree)


            implementation(projects.core.utils)

            // Core модули
            implementation(projects.core.domain)
            implementation(projects.core.network)
            implementation(projects.core.data)

        }
    }
}
