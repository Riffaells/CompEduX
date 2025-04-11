package platform

/**
 * Platform information provider that works across all supported platforms.
 * Uses expect/actual pattern for platform-specific implementations.
 */
expect object Platform {
    /**
     * Get the name of the current platform
     * @return Platform name (e.g. "Android", "JVM", "iOS", "WASM")
     */
    fun name(): String

    /**
     * Get the version information of the current platform
     * @return Version string (e.g. OS version, browser version)
     */
    fun version(): String

    /**
     * Get a detailed description of the current platform
     * @return Detailed platform description
     */
    fun description(): String

    /**
     * Get a string suitable for HTTP User-Agent header
     * @param appName The application name to include
     * @param appVersion The application version to include
     * @return A formatted User-Agent string
     */
    fun userAgent(appName: String, appVersion: String): String
}
