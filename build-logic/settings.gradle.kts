rootProject.name = "build-logic"

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.4.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":convention")
