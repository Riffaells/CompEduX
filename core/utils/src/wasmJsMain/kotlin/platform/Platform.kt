package platform

import kotlinx.browser.window

/**
 * WASM implementation of Platform
 */
actual object Platform {
    /**
     * Get the name of the current platform - WASM
     */
    actual fun name(): String = "WASM"

    /**
     * Get the browser and OS version information
     */
    actual fun version(): String {
        return try {
            val userAgent = window.navigator.userAgent
            when {
                userAgent.contains("Windows") -> "Windows"
                userAgent.contains("Mac") -> "macOS"
                userAgent.contains("Linux") -> "Linux"
                userAgent.contains("Android") -> "Android"
                userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad") -> "iOS"
                else -> "Unknown"
            }
        } catch (e: Throwable) {
            "Browser"
        }
    }

    /**
     * Get a detailed description of the current platform
     */
    actual fun description(): String {
        return try {
            "Browser: ${window.navigator.userAgent}"
        } catch (e: Throwable) {
            "WASM Browser environment"
        }
    }

    /**
     * Get a formatted User-Agent string
     * For WASM, we just return the browser's user agent plus app info
     */
    actual fun userAgent(appName: String, appVersion: String): String {
        return try {
            "$appName/$appVersion (WASM; ${window.navigator.userAgent})"
        } catch (e: Throwable) {
            "$appName/$appVersion (WASM)"
        }
    }
}
