package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.FlowContext
import dev.waylon.apexflow.core.FlowDsl
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.plugin.impl.LoggingPlugin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Top-level DSL function for creating ApexFlow workflows
 * Uses Kotlin 2.3.0 context receivers for improved DSL experience
 *
 * @param block Flow transformation function
 * @return Configured ApexFlow instance
 */
@FlowDsl
context(context: FlowContext)  // Kotlin 2.3.0 context receiver
fun <I, O> apexFlow(block: Flow<I>.() -> Flow<O>): ApexFlow<I, O> {
    return object : ApexFlow<I, O> {
        override fun transform(input: Flow<I>): Flow<O> {
            val transformedFlow = input.block()
            return if (context.debugMode) {
                transformedFlow.onEach { println("Debug mode: $it") }
            } else {
                transformedFlow
            }
        }
    }
}

/**
 * Overload: apexFlow without context receiver (backward compatible)
 */
@FlowDsl
fun <I, O> apexFlow(block: Flow<I>.() -> Flow<O>): ApexFlow<I, O> {
    return object : ApexFlow<I, O> {
        override fun transform(input: Flow<I>): Flow<O> {
            return input.block()
        }
    }
}

/**
 * Extension function: transformation operation with coroutine dispatcher
 * Leverages Kotlin 2.3.0's improved type inference
 */
@FlowDsl
fun <I, O> Flow<I>.transformOn(
    dispatcher: CoroutineDispatcher,
    block: suspend (I) -> O
): Flow<O> {
    return this.flowOn(dispatcher).map(block)
}

/**
 * Extension function: IO-intensive transformation operation
 */
@FlowDsl
fun <I, O> Flow<I>.transformOnIO(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.IO, block)
}

/**
 * Extension function: CPU-intensive transformation operation
 */
@FlowDsl
fun <I, O> Flow<I>.transformOnDefault(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Default, block)
}

/**
 * Extension function: transformation operation with unconfined dispatcher
 */
@FlowDsl
fun <I, O> Flow<I>.transformOnUnconfined(block: suspend (I) -> O): Flow<O> {
    return transformOn(Dispatchers.Unconfined, block)
}

/**
 * Modern extension: parallel transformation with concurrency limit
 * Uses Kotlin 2.3.0's improved Flow APIs
 */
@FlowDsl
context(context: FlowContext)  // Kotlin 2.3.0 context receiver
fun <I, O> Flow<I>.parallelTransform(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend (I) -> O
): Flow<O> {
    return this
        .flowOn(dispatcher)
        .map(block)
}

/**
 * Extension function: wrap flow with plugin
 */
@FlowDsl
fun <I, O> ApexFlow<I, O>.withPlugin(plugin: ApexFlowPlugin): ApexFlow<I, O> {
    return plugin.wrap(this)
}

/**
 * Extension function: add logging plugin
 * Uses SLF4J API, no dependency on specific implementation
 */
@FlowDsl
fun <I, O> ApexFlow<I, O>.withLogging(loggerName: String = "dev.waylon.apexflow"): ApexFlow<I, O> {
    return LoggingPlugin(loggerName).wrap(this)
}


/**
 * Modern Flow extension: conditional transformation
 * Kotlin 2.3.0 style extension function
 */
@FlowDsl
fun <T> Flow<T>.transformIf(
    condition: (T) -> Boolean,
    transform: suspend (T) -> T
): Flow<T> {
    return this.map { if (condition(it)) transform(it) else it }
}


