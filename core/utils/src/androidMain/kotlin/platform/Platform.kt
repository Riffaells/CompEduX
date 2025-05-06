@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package platform

import android.os.Build

/**
 * Android implementation of Platform
 */
actual object Platform {
    /**
     * Get the name of the current platform - Android
     */
    actual fun name(): String = "Android"

    /**
     * Get the Android version information
     */
    actual fun version(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    /**
     * Get a detailed description of the current platform
     */
    actual fun description(): String {
        return "Device: ${Build.MANUFACTURER} ${Build.MODEL}, " +
                "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), " +
                "Build: ${Build.DISPLAY}"
    }

    /**
     * Get a formatted User-Agent string for Android
     */
    actual fun userAgent(appName: String, appVersion: String): String {
        return "$appName/$appVersion (Android ${Build.VERSION.RELEASE}; " +
                "${Build.MANUFACTURER} ${Build.MODEL})"
    }
}
