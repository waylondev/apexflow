package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.util.createLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * Extension function: add timing functionality to standard Kotlin Flow
 *
 * Measures and logs the execution time of standard Kotlin Flow operations.
 * Uses SLF4J for logging execution duration.
 *
 * Usage Example:
 * ```kotlin
 * val standardFlow: Flow<Int> = flowOf(1, 2, 3)
 * val timedFlow = standardFlow.withTiming("my-standard-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.standard-flow.timing)
 * @return Flow instance with execution time measurement enabled
 */
fun <T> Flow<T>.withTiming(loggerName: String = "dev.waylon.apexflow.standard-flow.timing"): Flow<T> {
    val logger = createLogger(loggerName)
    var startTime: Long = 0

    return this
        .onStart {
            startTime = System.currentTimeMillis()
            logger.info("Flow execution started")
        }
        .catch { exception ->
            val duration = System.currentTimeMillis() - startTime
            logger.error("Flow execution failed after ${duration}ms", exception)
            throw exception
        }
        .onCompletion { cause ->
            val duration = System.currentTimeMillis() - startTime
            if (cause == null) {
                logger.info("Flow execution completed successfully in ${duration}ms")
            } else {
                logger.error("Flow execution completed with error after ${duration}ms", cause)
            }
        }
}
