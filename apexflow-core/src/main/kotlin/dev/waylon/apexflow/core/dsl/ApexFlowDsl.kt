package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import kotlinx.coroutines.flow.Flow

/**
 * Top-level DSL function for creating ApexFlow workflows
 *
 * This is the primary way to create ApexFlow instances, following the "Everything is Flow" principle.
 * The DSL block receives a Flow<I> receiver and must return a Flow<O>, ensuring type safety.
 *
 * Usage Example:
 * ```kotlin
 * val myFlow = apexFlow<Int, String> {
 *     transformOnIO { input ->
 *         "Processed: $input"
 *     }
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


