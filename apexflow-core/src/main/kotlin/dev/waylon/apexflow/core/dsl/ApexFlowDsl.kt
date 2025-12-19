package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.transform

/**
 * Top-level DSL function for creating ApexFlow workflows
 *
 * This is the primary way to create ApexFlow instances, following the "Everything is Flow" principle.
 * The DSL block receives a Flow<I> receiver and must return a Flow<O>, ensuring type safety.
 *
 * Usage Example:
 * ```kotlin
 * val myFlow = apexFlow<Int, String> {
 *     map { "Processed: $it" }
 * }
 *
 * // Complex workflow example
 * val complexFlow = apexFlow<Int, String> {
 *     filter { it > 0 }
 *     .map { it * 2 }
 *     .catch { emit("Error: ${it.message}") }
 * }
 * ```
 *
 * @param block Flow transformation function with Flow<I> as receiver
 * @return Configured ApexFlow instance
 *
 * @see [ApexFlow] for core interface documentation
 */
@ApexFlowDsl
inline fun <I, O> apexFlow(crossinline block: Flow<I>.() -> Flow<O>): ApexFlow<I, O> {
    return object : ApexFlow<I, O> {
        override fun transform(input: Flow<I>): Flow<O> {
            return input.block()
        }
    }
}

/**
 * Create a simple ApexFlow that maps input values
 */
@ApexFlowDsl
inline fun <I, O> mapFlow(crossinline mapper: (I) -> O): ApexFlow<I, O> {
    return apexFlow { map(mapper) }
}

/**
 * Create a simple ApexFlow that filters input values
 */
@ApexFlowDsl
inline fun <I> filterFlow(crossinline predicate: (I) -> Boolean): ApexFlow<I, I> {
    return apexFlow { filter(predicate) }
}

/**
 * Create a simple ApexFlow that retries failed operations
 */
@ApexFlowDsl
fun <I, O> retryFlow(maxRetries: Long = 3, delayMs: Long = 1000): ApexFlow<I, O> {
    return apexFlow { retry(maxRetries) }
}


