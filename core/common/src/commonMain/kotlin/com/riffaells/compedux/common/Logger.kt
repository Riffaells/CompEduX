package com.riffaells.compedux.common

import co.touchlab.kermit.Logger as KermitLogger

/**
 * Simple wrapper around Kermit logger for the CompEduX application
 */
object Logger {
    private val logger = KermitLogger.withTag("CompEduX")

    fun debug(message: String) {
        logger.d { message }
    }

    fun info(message: String) {
        logger.i { message }
    }

    fun warning(message: String) {
        logger.w { message }
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.e(throwable) { message }
        } else {
            logger.e { message }
        }
    }
}
