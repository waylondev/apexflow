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
 * Focused on "everything is Flow" principle - simple and pure
 *
 * @param block Flow transformation function
 * @return Configured ApexFlow instance
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
 * Core Flow transformation with dispatcher support
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
 */
@ApexFlowDsl
fun <I, O> Flow<I>.transformOnIO(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.IO, block)
}

/**
 * Extension function: CPU-intensive transformation operation
 */
@ApexFlowDsl
fun <I, O> Flow<I>.transformOnDefault(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Default, block)
}

/**
 * Extension function: wrap flow with plugin
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPlugin(plugin: ApexFlowPlugin): ApexFlow<I, O> {
    return plugin.wrap(this)
}

/**
 * Extension function: add logging plugin
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withLogging(loggerName: String = "dev.waylon.apexflow"): ApexFlow<I, O> {
    return LoggingPlugin(loggerName).wrap(this)
}

/**
 * Convenience extension function to execute ApexFlow
 * Provides a more readable API: flow.execute(inputFlow)
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.execute(input: Flow<I>): Flow<O> {
    return this.transform(input)
}

/**
 * DSL for branch handling in ApexFlow, inspired by Kotlin's `when` expression
 * Provides type-safe, readable branch logic while maintaining "Everything is Flow" principle
 *
 * Usage:
 * ```kotlin
 * val flow = apexFlow<Int, String> {
 *     whenFlow {
 *         case({ it > 10 }) {
 *             map { "Large: $it" }
 *         }
 *         case({ it > 5 }) {
 *             map { "Medium: $it" }
 *         }
 *         elseCase {
 *             map { "Small: $it" }
 *         }
 *     }
 * }
 * ```
 */
@ApexFlowDsl
fun <I, O> Flow<I>.whenFlow(
    configure: WhenFlowBuilder<I, O>.() -> Unit
): Flow<O> {
    val builder = WhenFlowBuilder<I, O>()
    builder.configure()

    return this.flatMapLatest { input ->
        builder.evaluate(input)
    }
}

/**
 * Builder class for the whenFlow DSL
 * Provides a simple API for defining branch cases and transformations
 */
@ApexFlowDsl
class WhenFlowBuilder<I, O> {
    private val cases = mutableListOf<Pair<(I) -> Boolean, (Flow<I>) -> Flow<O>>>()
    private var elseBranch: ((Flow<I>) -> Flow<O>)? = null

    /**
     * Define a branch case with a condition and transformation
     *
     * @param condition Predicate to match the input
     * @param transformation Flow transformation to apply if condition is met
     */
    fun case(condition: (I) -> Boolean, transformation: (Flow<I>) -> Flow<O>) {
        cases.add(condition to transformation)
    }

    /**
     * Define the else branch - executed if no other case matches
     *
     * @param transformation Flow transformation to apply as default
     */
    fun elseCase(transformation: (Flow<I>) -> Flow<O>) {
        this.elseBranch = transformation
    }

    /**
     * Evaluate the input against all cases and return the matching transformation
     *
     * @param input Input value to evaluate
     * @return Flow with the matching transformation applied
     */
    internal fun evaluate(input: I): Flow<O> {
        val matchingCase = cases.firstOrNull { it.first(input) }
        val transformation = matchingCase?.second ?: elseBranch
        ?: throw IllegalStateException("No matching case found for input: $input")

        return transformation(flow { emit(input) })
    }
}


