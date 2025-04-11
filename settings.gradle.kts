rootProject.name = "CompEduX"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
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
    }
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
)
