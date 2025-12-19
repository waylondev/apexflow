package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.dsl.withTiming
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import kotlinx.coroutines.flow.Flow

/**
 * Timing plugin for measuring flow execution time
 * This plugin records the execution time of each flow and logs it using SLF4J
 */
class ApexTimingPlugin(
    private val loggerName: String = "dev.waylon.apexflow.timing"
) : ApexFlowPlugin {

    private val logger = createLogger(loggerName)

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                return input
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .withTiming(loggerName)
            }
        }
    }
}
