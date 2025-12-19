package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * Core logging plugin for ApexFlow
 * Provides SLF4J-based logging functionality for flow execution
 *
 * This is a core plugin provided by the framework, demonstrating how to implement ApexFlowPlugin
 * Clients can use this plugin directly or as a reference for creating custom plugins
 */
class ApexLoggingPlugin(private val loggerName: String = "dev.waylon.apexflow") : ApexFlowPlugin {

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        // Create SLF4J logger
        val logger = createLogger(loggerName)

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                // Use SLF4J to record logs at different stages of flow execution
                return input
                    .onStart { logger.info("Flow execution started") }
                    .onEach { data: I -> logger.debug("Processing input: {}", data) }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onEach { data: O -> logger.debug("Processed output: {}", data) }
                    .catch { e -> logger.error("Flow execution failed with exception", e) }
                    .onCompletion { exception: Throwable? ->
                        if (exception == null) {
                            logger.info("Flow execution completed successfully")
                        } else {
                            logger.error("Flow execution completed with exception", exception)
                        }
                    }
            }
        }
    }
}
