package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.dsl.withPluginTiming
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import kotlinx.coroutines.flow.Flow

/**
 * Timing plugin for measuring flow execution time
 * This plugin records the execution time of each flow and logs it using SLF4J
 */
class ApexTimingPlugin(
    private val loggerName: String = "dev.waylon.apexflow.plugin.timing"
) : ApexFlowPlugin {

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                return input
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .withPluginTiming(loggerName)
            }
        }
    }
}
