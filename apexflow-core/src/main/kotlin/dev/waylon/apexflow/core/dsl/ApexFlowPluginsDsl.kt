package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowDsl
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexLoggingPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexPerformanceMonitoringPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexTimingPlugin

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
    return withPlugin(ApexLoggingPlugin(loggerName))
}

/**
 * Extension function: add timing plugin
 *
 * Convenience function for adding execution time measurement to ApexFlow instances.
 * Measures and logs the execution time of flow transformations.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val timedFlow = flow.withTiming("my-timed-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.timing)
 * @return ApexFlow instance with execution time measurement enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withTiming(loggerName: String = "dev.waylon.apexflow.timing"): ApexFlow<I, O> {
    return withPlugin(ApexTimingPlugin(loggerName))
}

/**
 * Extension function: add performance monitoring plugin
 *
 * Convenience function for adding system resource monitoring to ApexFlow instances.
 * Tracks CPU usage, memory consumption, and thread count during flow execution.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val monitoredFlow = flow.withPerformanceMonitoring("my-monitored-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.performance)
 * @return ApexFlow instance with performance monitoring enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPerformanceMonitoring(loggerName: String = "dev.waylon.apexflow.performance"): ApexFlow<I, O> {
    return withPlugin(ApexPerformanceMonitoringPlugin(loggerName))
}
