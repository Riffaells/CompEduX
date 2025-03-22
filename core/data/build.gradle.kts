plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Data модуль должен зависеть от domain согласно принципам чистой архитектуры
            implementation(projects.core.domain)
        }
    }
}
