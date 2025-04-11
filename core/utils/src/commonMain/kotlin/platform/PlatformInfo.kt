package platform

/**
 * Helper class with platform-specific information and utility functions.
 * Provides easy access to platform details across all supported targets.
 */
object PlatformInfo {
    /**
     * The name of the current platform (e.g. "JVM", "Android", "iOS", "WASM")
     */
    val name: String
        get() = Platform.name()

    /**
     * The version of the current platform
     */
    val version: String
        get() = Platform.version()

    /**
     * A detailed description of the current platform
     */
    val description: String
        get() = Platform.description()

    /**
     * Checks if the current platform is JVM (desktop)
     */
    val isJvm: Boolean
        get() = name == "JVM"

    /**
     * Checks if the current platform is Android
     */
    val isAndroid: Boolean
        get() = name == "Android"

    /**
     * Checks if the current platform is iOS
     */
    val isIos: Boolean
        get() = name == "iOS"

    /**
     * Checks if the current platform is WASM
     */
    val isWasm: Boolean
        get() = name == "WASM"

    /**
     * Checks if the current platform is mobile (Android or iOS)
     */
    val isMobile: Boolean
        get() = isAndroid || isIos

    /**
     * Checks if the current platform is desktop (JVM)
     */
    val isDesktop: Boolean
        get() = isJvm

    /**
     * Checks if the current platform is web (WASM)
     */
    val isWeb: Boolean
        get() = isWasm

    /**
     * Creates a User-Agent string for HTTP requests
     * @param appName The name of the application
     * @param appVersion The version of the application
     * @return A formatted User-Agent string
     */
    fun createUserAgent(appName: String, appVersion: String): String {
        return Platform.userAgent(appName, appVersion)
    }

    /**
     * Gets a shortened platform identifier suitable for analytics or logging
     * @return A short platform identifier (e.g. "jvm", "and", "ios", "web")
     */
    fun getPlatformId(): String {
        return when {
            isJvm -> "jvm"
            isAndroid -> "and"
            isIos -> "ios"
            isWasm -> "web"
            else -> "unk"
        }
    }
}
