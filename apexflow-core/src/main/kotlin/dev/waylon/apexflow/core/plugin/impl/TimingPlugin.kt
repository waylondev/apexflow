package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory

/**
 * Timing plugin for measuring flow execution time
 * This plugin records the execution time of each flow and logs it using SLF4J
 */
class TimingPlugin(
    private val loggerName: String = "dev.waylon.apexflow.timing"
) : ApexFlowPlugin {

    private val logger = LoggerFactory.getLogger(loggerName)

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                // Use a mutable ref that's scoped to this transform call
                var startTime: Long = 0

                return input
                    .onStart { startTime = System.currentTimeMillis() }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .catch { exception ->
                        val endTime = System.currentTimeMillis()
                        val duration = endTime - startTime
                        logger.error("Flow execution failed after ${duration}ms", exception)
                        throw exception
                    }
                    .onCompletion {
                        val endTime = System.currentTimeMillis()
                        val duration = endTime - startTime
                        logger.info("Flow execution completed in ${duration}ms")
                    }
            }
        }
    }
}
