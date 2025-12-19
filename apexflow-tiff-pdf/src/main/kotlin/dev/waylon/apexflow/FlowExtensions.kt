package dev.waylon.apexflow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Extension function to enable parallel processing of Flow elements
 *
 * This extension provides a simple way to process Flow elements in parallel
 * while maintaining the order of elements. It uses Kotlin Flow's flatMapMerge operator
 * which allows concurrent processing of multiple elements while preserving the original
 * sequence of the flow.
 *
 * @param parallelism Maximum number of concurrent operations to execute
 * @param processor Function to process each element
 * @return Flow with processed elements in original order
 */
fun <T, R> Flow<T>.withParallelProcessing(
    parallelism: Int = Runtime.getRuntime().availableProcessors(),
    processor: suspend (T) -> R
): Flow<R> {
    return this.flatMapMerge(parallelism) {
        flow { emit(processor(it)) }.flowOn(Dispatchers.IO)
    }
}

/**
 * Extension function to enable parallel processing of indexed Flow elements
 *
 * @param parallelism Maximum number of concurrent operations to execute
 * @param processor Function to process each element with its index
 * @return Flow with processed elements in original order
 */
fun <T, R> Flow<T>.withParallelIndexedProcessing(
    parallelism: Int = Runtime.getRuntime().availableProcessors(),
    processor: suspend (Int, T) -> R
): Flow<R> {
    return this.withIndex()
        .flatMapMerge(parallelism) { (index, value) ->
            flow { emit(processor(index, value)) }.flowOn(Dispatchers.IO)
        }
}

/**
 * Extension function to create a parallel flow from a sequence
 *
 * @param parallelism Maximum number of concurrent operations to execute
 * @param processor Function to process each element
 * @return Flow with processed elements in original order
 */
fun <T, R> Iterable<T>.asParallelFlow(
    parallelism: Int = Runtime.getRuntime().availableProcessors(),
    processor: suspend (T) -> R
): Flow<R> {
    return this.asFlow()
        .flatMapMerge(parallelism) {
            flow { emit(processor(it)) }.flowOn(Dispatchers.IO)
        }
}
