rootProject.name = "build-logic"

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.4.1"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":convention")
