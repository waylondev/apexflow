package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.plugin.impl.LoggingPlugin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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


