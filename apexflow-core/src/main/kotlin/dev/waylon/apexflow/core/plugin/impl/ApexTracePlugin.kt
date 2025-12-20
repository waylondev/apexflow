package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * Trace plugin for ApexFlow
 *
 * Provides detailed execution tracing, logging each component's execution flow,
 * including input/output samples, timestamps, and execution context.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val tracedFlow = flow.withPluginTrace()
 * ```
 */
class ApexTracePlugin(private val loggerName: String = "dev.waylon.apexflow.plugin.trace") : ApexFlowPlugin {

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        val logger = createLogger(loggerName)
        val flowId = "flow-${System.currentTimeMillis()}"

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                val componentName = flow.javaClass.simpleName.ifEmpty { "AnonymousComponent" }

                return input
                    .onStart {
                        logger.info("[Flow: $flowId] Component '$componentName' started at ${Instant.now()}")
                    }
                    .onEach { data: I ->
                        logger.trace("[Flow: $flowId] Component '$componentName' processing input: $data")
                    }
                    .let { originalFlow ->
                        logger.debug("[Flow: $flowId] Component '$componentName' executing main logic")
                        flow.transform(originalFlow)
                    }
                    .onEach { data: O ->
                        logger.trace("[Flow: $flowId] Component '$componentName' produced output: $data")
                    }
                    .catch { e ->
                        logger.error("[Flow: $flowId] Component '$componentName' failed at ${Instant.now()}", e)
                    }
                    .onCompletion { exception: Throwable? ->
                        if (exception == null) {
                            logger.info("[Flow: $flowId] Component '$componentName' completed successfully at ${Instant.now()}")
                        } else {
                            logger.error(
                                "[Flow: $flowId] Component '$componentName' completed with exception at ${Instant.now()}",
                                exception
                            )
                        }
                    }
            }
        }
    }
}


