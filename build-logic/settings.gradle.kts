rootProject.name = "build-logic"

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.7.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":convention")
