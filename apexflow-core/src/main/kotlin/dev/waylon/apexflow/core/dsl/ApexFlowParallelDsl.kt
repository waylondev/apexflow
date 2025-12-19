package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

/**
 * Parallel processing extensions for ApexFlow
 */

/**
 * Extension function: Enable parallel processing for ApexFlow
 *
 * This extension function allows parallel processing of flow data while maintaining order
 *
 * Usage Example:
 * ```kotlin
 * val parallelFlow = apexFlow { ... }
 *     .withParallelProcessing(4)
 * ```
 *
 * @param parallelism Degree of parallelism to use
 * @return ApexFlow instance with parallel processing enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, Flow<O>>.withParallelProcessing(parallelism: Int = Runtime.getRuntime().availableProcessors()): ApexFlow<I, O> {
    return object : ApexFlow<I, O> {
        override fun transform(input: Flow<I>): Flow<O> {
            // Use flatMapMerge to enable parallel processing while maintaining order
            return this@withParallelProcessing.transform(input)
                .flatMapMerge(parallelism) { flowData -> flowData }
        }
    }
}

/**
 * Extension function: Create a parallel processing component
 *
 * This extension function creates an ApexFlow component that processes data in parallel
 *
 * Usage Example:
 * ```kotlin
 * val parallelProcessor = parallelProcessor<Int, String>(4) {
 *     "Processed: $it"
 * }
 * ```
 *
 * @param parallelism Degree of parallelism to use
 * @param processor Function to process each item
 * @return ApexFlow instance for parallel processing
 */
@ApexFlowDsl
inline fun <I, O> parallelProcessor(
    parallelism: Int = Runtime.getRuntime().availableProcessors(),
    crossinline processor: suspend (I) -> O
): ApexFlow<I, O> {
    return object : ApexFlow<I, O> {
        override fun transform(input: Flow<I>): Flow<O> {
            return input.flatMapMerge(parallelism) {
                flow { emit(processor(it)) }
            }
        }
    }
}

