plugins {
    alias(libs.plugins.compedux.multiplatform)
    alias(libs.plugins.compedux.multiplatform.library)
}

dependencies {
    commonMainImplementation(projects.core.data)
}
