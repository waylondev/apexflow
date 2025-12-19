package dev.waylon.apexflow.core.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Unified logging utility class
 *
 * Provides concise logging creation methods, eliminating repetitive LoggerFactory.getLogger calls
 * Follows Kotlin best practices using extension functions and properties
 */

/**
 * Extension property: provides logger property for any class
 *
 * Usage example:
 * ```kotlin
 * class MyClass {
 *     private val logger = logger
 *
 *     fun myMethod() {
 *         logger.info("Method called")
 *     }
 * }
 * ```
 */
val <T : Any> T.logger: Logger
    get() = LoggerFactory.getLogger(this::class.java)

/**
 * Extension function: creates logger for a class
 *
 * Usage example:
 * ```kotlin
 * class MyClass {
 *     private val logger = createLogger<MyClass>()
 * }
 * ```
 */
inline fun <reified T> createLogger(): Logger = LoggerFactory.getLogger(T::class.java)

/**
 * Top-level function: creates logger with specified name
 *
 * Usage example:
 * ```kotlin
 * private val logger = createLogger("custom-logger-name")
 * ```
 */
fun createLogger(name: String): Logger = LoggerFactory.getLogger(name)

/**
 * Safe logging extension functions
 *
 * Provides null-safe logging methods to avoid null pointer exceptions
 */

/**
 * Safely logs debug information, only executes computation when debug level is enabled
 */
inline fun Logger.debugSafe(lazyMessage: () -> String) {
    if (isDebugEnabled) {
        debug(lazyMessage())
    }
}

/**
 * Safely logs trace information, only executes computation when trace level is enabled
 */
inline fun Logger.traceSafe(lazyMessage: () -> String) {
    if (isTraceEnabled) {
        trace(lazyMessage())
    }
}

/**
 * Context-aware logging
 *
 * Adds context information to log messages for easier debugging and tracing
 */
fun Logger.infoWithContext(message: String, context: Map<String, Any?>) {
    val contextStr = context.entries
        .filter { it.value != null }
        .joinToString(", ") { "${it.key}=${it.value}" }
    info("$message [$contextStr]")
}

fun Logger.debugWithContext(message: String, context: Map<String, Any?>) {
    if (isDebugEnabled) {
        val contextStr = context.entries
            .filter { it.value != null }
            .joinToString(", ") { "${it.key}=${it.value}" }
        debug("$message [$contextStr]")
    }
}

/**
 * Performance monitoring related logging utilities
 */

/**
 * Measures and logs method execution time
 */
inline fun <T> Logger.measureTime(operationName: String, block: () -> T): T {
    val startTime = System.currentTimeMillis()
    return try {
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        debug("$operationName completed in ${duration}ms")
        result
    } catch (e: Exception) {
        val duration = System.currentTimeMillis() - startTime
        error("$operationName failed after ${duration}ms", e)
        throw e
    }
}

/**
 * Flow processing related logging utilities
 */

/**
 * Logs Flow processing start
 */
fun Logger.flowStarted(flowName: String, context: Map<String, Any?> = emptyMap()) {
    infoWithContext("Flow started: $flowName", context)
}

/**
 * Logs Flow processing completion
 */
fun Logger.flowCompleted(flowName: String, duration: Long, context: Map<String, Any?> = emptyMap()) {
    infoWithContext("Flow completed: $flowName (${duration}ms)", context)
}

/**
 * Logs Flow processing error
 */
fun Logger.flowError(flowName: String, error: Throwable, context: Map<String, Any?> = emptyMap()) {
    errorWithContext("Flow error: $flowName", error, context)
}

/**
 * Error logging with context
 */
fun Logger.errorWithContext(message: String, error: Throwable, context: Map<String, Any?>) {
    val contextStr = context.entries
        .filter { it.value != null }
        .joinToString(", ") { "${it.key}=${it.value}" }
    error("$message [$contextStr]", error)
}