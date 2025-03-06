package com.compedu.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the

/**
 * Extension to access the version catalog from convention plugins
 */
internal val Project.libs: VersionCatalog
    get() = the<VersionCatalogsExtension>().named("libs")
