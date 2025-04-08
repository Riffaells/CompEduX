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

            // Core модули
            implementation(projects.core.domain)
            implementation(projects.core.network)
            implementation(projects.core.data)

            // Добавляем essenty-lifecycle-coroutines для правильной работы с корутинами
            implementation(libs.essenty.lifecycle.coroutines)

            // common не должен зависеть от ui и design,
            // напротив, ui зависит от common и design
        }
    }
}
