plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
    alias(libs.plugins.compedux.multiplatform.compose)
    alias(libs.plugins.compedux.decompose)
    alias(libs.plugins.compedux.kconfig)
}
//
//kotlin {
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//        binaries.executable()
//    }
//
//    // Остальная конфигурация, которую добавляет multiplatform-library плагин
//}
