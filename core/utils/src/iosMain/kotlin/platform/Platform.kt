package platform

import platform.UIKit.UIDevice

/**
 * iOS implementation of Platform
 */
actual object Platform {
    /**
     * Get the name of the current platform - iOS
     */
    actual fun name(): String = "iOS"

    /**
     * Get the iOS version information
     */
    actual fun version(): String {
        return "iOS ${UIDevice.currentDevice.systemVersion}"
    }

    /**
     * Get a detailed description of the current platform
     */
    actual fun description(): String {
        val device = UIDevice.currentDevice
        return "Device: ${device.model}, " +
                "Name: ${device.name}, " +
                "System: ${device.systemName} ${device.systemVersion}"
    }

    /**
     * Get a formatted User-Agent string for iOS
     */
    actual fun userAgent(appName: String, appVersion: String): String {
        val device = UIDevice.currentDevice
        return "$appName/$appVersion (iOS ${device.systemVersion}; ${device.model})"
    }
}
