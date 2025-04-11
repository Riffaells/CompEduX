package platform

/**
 * JVM implementation of Platform
 */
actual object Platform {
    /**
     * Get the name of the current platform - JVM
     */
    actual fun name(): String = "JVM"

    /**
     * Get the JVM and OS version information
     */
    actual fun version(): String {
        val osName = System.getProperty("os.name") ?: "Unknown"
        val osVersion = System.getProperty("os.version") ?: ""
        val javaVersion = System.getProperty("java.version") ?: ""

        return "Java $javaVersion, $osName $osVersion"
    }

    /**
     * Get a detailed description of the current platform
     */
    actual fun description(): String {
        val osName = System.getProperty("os.name") ?: "Unknown"
        val osVersion = System.getProperty("os.version") ?: ""
        val osArch = System.getProperty("os.arch") ?: ""
        val javaVendor = System.getProperty("java.vendor") ?: ""
        val javaVersion = System.getProperty("java.version") ?: ""
        val javaVmName = System.getProperty("java.vm.name") ?: ""

        return "OS: $osName $osVersion ($osArch), " +
               "Java: $javaVersion from $javaVendor, " +
               "VM: $javaVmName"
    }

    /**
     * Get a formatted User-Agent string
     */
    actual fun userAgent(appName: String, appVersion: String): String {
        val osName = System.getProperty("os.name")?.replace(" ", "") ?: "Unknown"
        val osVersion = System.getProperty("os.version") ?: ""
        val javaVersion = System.getProperty("java.version") ?: ""

        return "$appName/$appVersion ($osName $osVersion; Java $javaVersion)"
    }
}
