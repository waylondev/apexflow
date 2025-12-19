package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Convenience extension function to execute ApexFlow
 *
 * Provides a more readable API for executing ApexFlow workflows.
 * This is a simple wrapper around the transform() method.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val input = flowOf(1, 2, 3)
 * val result = flow.execute(input).toList()
 * ```
 *
 * @param input Input Flow to process
 * @return Output Flow with processed data
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.execute(input: Flow<I>): Flow<O> {
    return this.transform(input)
}

/**
 * Convenience extension function to execute ApexFlow with a single value
 *
 * Provides a more readable API for executing ApexFlow workflows with immediate values.
 * This is a wrapper around the transform() method that creates a single-element Flow.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val result = flow.execute(42).toList()
 * ```
 *
 * @param value Single input value to process
 * @return Output Flow with processed data
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.execute(value: I): Flow<O> {
    return this.transform(flowOf(value))
}

/**
 * Convenience extension function to execute ApexFlow with a list of values
 *
 * Provides a more readable API for executing ApexFlow workflows with multiple values.
 * This is a wrapper around the transform() method that creates a Flow from the list.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val result = flow.execute(listOf(1, 2, 3)).toList()
 * ```
 *
 * @param values List of input values to process
 * @return Output Flow with processed data
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.execute(values: List<I>): Flow<O> {
    return this.transform(values.asFlow())
}

/**
 * Convenience extension function to execute ApexFlow with varargs values
 *
 * Provides a more readable API for executing ApexFlow workflows with multiple values.
 * This is a wrapper around the transform() method that creates a Flow from varargs.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val result = flow.execute(1, 2, 3).toList()
 * ```
 *
 * @param values Varargs of input values to process
 * @return Output Flow with processed data
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.execute(vararg values: I): Flow<O> {
    return this.transform(values.toList().asFlow())
}
