package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexLoggingPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexPerformanceMonitoringPlugin

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
fun <I, O> ApexFlow<I, O>.withLogging(loggerName: String = "dev.waylon.apexflow.plugin.logging"): ApexFlow<I, O> {
    return withPlugin(ApexLoggingPlugin(loggerName))
}

/**
 * Extension function: add timing functionality to ApexFlow
 *
 * Measures and logs the execution time of ApexFlow operations.
 * Uses SLF4J for logging execution duration.
 *
 * Usage Example:
 * ```kotlin
 * val myFlow = apexFlow { ... }
 * val timedFlow = myFlow.withTiming("my-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.flow-timing)
 * @return ApexFlow instance with execution time measurement enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withTiming(loggerName: String = "dev.waylon.apexflow.plugin.timing"): ApexFlow<I, O> {
    return this.withPlugin(ApexLoggingPlugin(loggerName))
}

/**
 * Extension function: add performance monitoring plugin
 *
 * Convenience function for adding comprehensive performance monitoring
 * to ApexFlow instances. Tracks CPU, memory, thread, and GC metrics.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val monitoredFlow = flow.withPerformanceMonitoring("my-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.performance)
 * @param samplingIntervalMs Sampling interval for metrics (default: 5000ms)
 * @param enableDetailedMetrics Whether to enable detailed metrics (default: false)
 * @return ApexFlow instance with performance monitoring enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPerformanceMonitoring(
    loggerName: String = "dev.waylon.apexflow.plugin.performance",
    samplingIntervalMs: Long = 5000,
    enableDetailedMetrics: Boolean = false
): ApexFlow<I, O> {
    return withPlugin(ApexPerformanceMonitoringPlugin(loggerName, samplingIntervalMs, enableDetailedMetrics))
}


