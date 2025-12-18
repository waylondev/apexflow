package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.plugin.impl.LoggingPlugin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

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
fun <I, O> apexFlow(block: Flow<I>.() -> Flow<O>): ApexFlow<I, O> {
    return object : ApexFlow<I, O> {
        override fun transform(input: Flow<I>): Flow<O> {
            return input.block()
        }
    }
}

/**
 * Simple transformation operation with coroutine dispatcher
 *
 * This function combines flowOn() and map() into a single convenient operation,
 * allowing explicit dispatcher specification for transformations.
 *
 * Usage Example:
 * ```kotlin
 * flow.transformOn(Dispatchers.IO) { input ->
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
fun <I, O> Flow<I>.transformOn(
    dispatcher: CoroutineDispatcher,
    block: suspend (I) -> O
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
 * flow.transformOnIO { input ->
 *     // File I/O or network operation
 *     readFromDatabase(input)
 * }
 * ```
 *
 * @param block IO-intensive transformation function
 * @return Flow with transformation applied
 */
@ApexFlowDsl
fun <I, O> Flow<I>.transformOnIO(block: suspend (I) -> O): Flow<O> {
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
 * flow.transformOnDefault { input ->
 *     // CPU-intensive calculation
 *     complexCalculation(input)
 * }
 * ```
 *
 * @param block CPU-intensive transformation function
 * @return Flow with transformation applied
 */
@ApexFlowDsl
fun <I, O> Flow<I>.transformOnDefault(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Default, block)
}

/**
 * Extension function: wrap flow with plugin
 *
 * Allows adding functionality to ApexFlow instances through plugins.
 * This follows the Decorator Pattern, enabling flexible functionality extension.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val pluginFlow = flow.withPlugin(CustomPlugin())
 * ```
 *
 * @param plugin Plugin to wrap the flow with
 * @return ApexFlow instance with plugin applied
 *
 * @see [ApexFlowPlugin] for plugin interface documentation
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPlugin(plugin: ApexFlowPlugin): ApexFlow<I, O> {
    return plugin.wrap(this)
}

/**
 * Extension function: add logging plugin
 *
 * Convenience function for adding logging functionality to ApexFlow instances.
 * Uses SLF4J for logging at different flow stages.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val loggedFlow = flow.withLogging("my-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow)
 * @return ApexFlow instance with logging enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withLogging(loggerName: String = "dev.waylon.apexflow"): ApexFlow<I, O> {
    return LoggingPlugin(loggerName).wrap(this)
}

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
 * DSL for branch handling in ApexFlow, inspired by Kotlin's `when` expression
 *
 * Provides type-safe, readable branch logic while maintaining the "Everything is Flow" principle.
 * This DSL allows defining conditional branches with different transformations, similar to a switch-case statement
 * but with Flow transformations as outcomes.
 *
 * Usage Example:
 * ```kotlin
 * val workflow = apexFlow<Int, String> {
 *     whenFlow {
 *         // First branch: numbers greater than 10
 *         case({ it > 10 }) {
 *             transformOnIO { "Large: $it (processed on IO)" }
 *         }
 *
 *         // Second branch: numbers greater than 5
 *         case({ it > 5 }) {
 *             map { "Medium: $it" }
 *         }
 *
 *         // Fallback branch: all other numbers
 *         elseCase {
 *             map { "Small: $it" }
 *         }
 *     }
 * }
 * ```
 *
 * @param configure Lambda to configure the branch cases
 * @return Flow with branch logic applied
 */
@ApexFlowDsl
fun <I, O> Flow<I>.whenFlow(
    configure: WhenFlowBuilder<I, O>.() -> Unit
): Flow<O> {
    val builder = WhenFlowBuilder<I, O>()
    builder.configure()

    // Use flatMapLatest for better performance with rapidly changing inputs
    // This cancels transformations for old values when new values arrive
    return this.flatMapLatest { input ->
        builder.evaluate(input)
    }
}

/**
 * Builder class for the whenFlow DSL
 *
 * This class follows the Builder Pattern, providing a fluent API for defining branch cases
 * and their corresponding transformations.
 *
 * @param I Input type of the Flow
 * @param O Output type of the Flow
 */
@ApexFlowDsl
class WhenFlowBuilder<I, O> {
    // Use private mutable list to store cases, ensuring encapsulation
    private val cases = mutableListOf<Pair<(I) -> Boolean, (Flow<I>) -> Flow<O>>>()
    private var elseBranch: ((Flow<I>) -> Flow<O>)? = null

    /**
     * Define a branch case with a condition and transformation
     *
     * The first matching case will be executed. Cases are evaluated in the order they are defined.
     *
     * @param condition Predicate function that determines if this case matches
     * @param transformation Flow transformation to apply if the condition is met
     */
    fun case(condition: (I) -> Boolean, transformation: (Flow<I>) -> Flow<O>) {
        cases.add(condition to transformation)
    }

    /**
     * Define the else branch - executed if no other case matches
     *
     * This provides a fallback transformation for all inputs that don't match any case.
     * If no elseCase is defined and no cases match, an IllegalStateException will be thrown.
     *
     * @param transformation Flow transformation to apply as the default case
     */
    fun elseCase(transformation: (Flow<I>) -> Flow<O>) {
        this.elseBranch = transformation
    }

    /**
     * Evaluate the input against all cases and return the matching transformation
     *
     * This method is internal and should not be called directly by users.
     *
     * @param input Input value to evaluate against the cases
     * @return Flow with the matching transformation applied
     * @throws IllegalStateException if no case matches and no elseCase is defined
     */
    internal fun evaluate(input: I): Flow<O> {
        // Find the first matching case
        val matchingCase = cases.firstOrNull { it.first(input) }

        // Get the transformation, or use elseBranch if no match found
        val transformation = matchingCase?.second ?: elseBranch
        ?: throw IllegalStateException("No matching case found for input: $input")

        // Create a single-element Flow for the input and apply the transformation
        return transformation(flow { emit(input) })
    }
}


