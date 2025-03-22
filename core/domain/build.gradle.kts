plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
}

// Домен не должен зависеть от других модулей согласно принципам чистой архитектуры
// Эта зависимость противоречит архитектуре и должна быть удалена
