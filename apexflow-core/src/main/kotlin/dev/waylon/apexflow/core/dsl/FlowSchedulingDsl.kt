package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlowDsl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Simple transformation operation with coroutine dispatcher
 *
 * This function combines flowOn() and map() into a single convenient operation,
 * allowing explicit dispatcher specification for transformations.
 *
 * Usage Example:
 * ```kotlin
 * flow.transformOn(Dispatchers.IO) {
 *     // IO-intensive operation
 *     input.toString()
 * }
 * ```
 *
 * @param dispatcher CoroutineDispatcher to run the transformation on
 * @param block Transformation function that runs on the specified dispatcher
 * @return Flow with transformation applied
 */
@ApexFlowDsl
inline fun <I, O> Flow<I>.transformOn(
    dispatcher: CoroutineDispatcher,
    crossinline block: suspend (I) -> O
): Flow<O> {
    return this.flowOn(dispatcher).map(block)
}

/**
 * Extension function: IO-intensive transformation operation
 *
 * Convenience function for IO-bound operations (file I/O, network calls, database queries).
 * Runs on Dispatchers.IO dispatcher.
 *
 * Usage Example:
 * ```kotlin
 * flow.transformOnIO {
 *     // File I/O or network operation
 *     readFromDatabase(input)
 * }
 * ```
 *
 * @param block IO-intensive transformation function
 * @return Flow with transformation applied
 */
@ApexFlowDsl
inline fun <I, O> Flow<I>.transformOnIO(crossinline block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.IO, block)
}

/**
 * Extension function: CPU-intensive transformation operation
 *
 * Convenience function for CPU-bound operations (calculations, computations).
 * Runs on Dispatchers.Default dispatcher.
 *
 * Usage Example:
 * ```kotlin
 * flow.transformOnDefault {
 *     // CPU-intensive calculation
 *     complexCalculation(input)
 * }
 * ```
 *
 * @param block CPU-intensive transformation function
 * @return Flow with transformation applied
 */
@ApexFlowDsl
inline fun <I, O> Flow<I>.transformOnDefault(crossinline block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Default, block)
}
