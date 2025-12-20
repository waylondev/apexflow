package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.util.createLogger
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
fun <T> Flow<T>.withPluginTiming(loggerName: String = "dev.waylon.apexflow.standard-flow.timing"): Flow<T> {
    val logger = createLogger(loggerName)
    var startTime: Long = 0

    return this
        .onStart {
            startTime = System.nanoTime()
            logger.info("Flow execution started")
        }
        .catch { exception ->
            val duration = (System.nanoTime() - startTime).toDuration(DurationUnit.NANOSECONDS)
            logger.error("Flow execution failed after $duration", exception)
            throw exception
        }
        .onCompletion { cause ->
            val duration = (System.nanoTime() - startTime).toDuration(DurationUnit.NANOSECONDS)
            if (cause == null) {
                logger.info("Flow execution completed successfully in $duration")
            } else {
                logger.error("Flow execution completed with error after $duration", cause)
            }
        }
}

/**
 * Extension function: Enable parallel processing of Flow elements
 *
 * This extension provides a simple way to process Flow elements in parallel
 * while maintaining the order of elements. It uses Kotlin Flow's flatMapMerge operator
 * which allows concurrent processing of multiple elements while preserving the original
 * sequence of the flow.
 *
 * Usage Example:
 * ```kotlin
 * val standardFlow: Flow<Int> = flowOf(1, 2, 3)
 * val parallelFlow = standardFlow.withParallelProcessing(4) {
 *     it * 2
 * }
 * ```
 *
 * @param parallelism Maximum number of concurrent operations to execute
 * @param processor Function to process each element
 * @return Flow with processed elements in original order
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T, R> Flow<T>.withParallelProcessing(
    parallelism: Int = Runtime.getRuntime().availableProcessors(),
    processor: suspend (T) -> R
): Flow<R> {
    return this.flatMapMerge(parallelism) {
        flow { emit(processor(it)) }.flowOn(Dispatchers.IO)
    }
}

/**
 * Extension function: Create a parallel flow from a sequence
 *
 * This extension function creates a Flow from an Iterable and processes its elements in parallel
 * while maintaining the original order.
 *
 * Usage Example:
 * ```kotlin
 * val parallelFlow = (1..100).asParallelFlow(4) {
 *     "Processed: $it"
 * }
 * ```
 *
 * @param parallelism Maximum number of concurrent operations to execute
 * @param processor Function to process each element
 * @return Flow with processed elements in original order
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T, R> Iterable<T>.asParallelFlow(
    parallelism: Int = Runtime.getRuntime().availableProcessors(),
    processor: suspend (T) -> R
): Flow<R> {
    return this.asFlow()
        .flatMapMerge(parallelism) {
            flow { emit(processor(it)) }.flowOn(Dispatchers.IO)
        }
}
