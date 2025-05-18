rootProject.name = "CompEduX"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
    }


}

// Делаем версионный каталог доступным для build-logic
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

// Включаем все модули проекта
include(
    "app",
    ":core:utils",
    ":core:common",
    ":core:data",
    ":core:domain",
    ":core:database",
    ":core:network",
    ":core:notifications",
    ":core:design",
    ":core:ui",
    ":feature:settings",
    ":feature:tree",
    ":feature:course",
)
