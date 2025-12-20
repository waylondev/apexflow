package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.ApexFlowConstants
import dev.waylon.apexflow.core.ApexFlowDsl
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexLoggingPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexPerformanceMonitoringPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexSlowOperationDetectorPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexThroughputPlugin
import dev.waylon.apexflow.core.plugin.impl.ApexTracePlugin
import java.time.Duration

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
 * val loggedFlow = flow.withPluginLogging("my-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow)
 * @return ApexFlow instance with logging enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPluginLogging(loggerName: String = "${ApexFlowConstants.APEXFLOW_NAMESPACE}.plugin.logging"): ApexFlow<I, O> {
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
 * val timedFlow = myFlow.withPluginTiming("my-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.flow-timing)
 * @return ApexFlow instance with execution time measurement enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPluginTiming(loggerName: String = "${ApexFlowConstants.APEXFLOW_NAMESPACE}.plugin.timing"): ApexFlow<I, O> {
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
 * val monitoredFlow = flow.withPluginPerformanceMonitoring("my-flow")
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.performance)
 * @param samplingIntervalMs Sampling interval for metrics (default: 5000ms)
 * @param enableDetailedMetrics Whether to enable detailed metrics (default: false)
 * @return ApexFlow instance with performance monitoring enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPluginPerformanceMonitoring(
    loggerName: String = "${ApexFlowConstants.APEXFLOW_NAMESPACE}.plugin.performance",
    samplingIntervalMs: Long = 5000,
    enableDetailedMetrics: Boolean = false
): ApexFlow<I, O> {
    return withPlugin(ApexPerformanceMonitoringPlugin(loggerName, samplingIntervalMs, enableDetailedMetrics))
}

/**
 * Extension function: add trace plugin
 *
 * Provides detailed execution tracing, logging each component's execution flow,
 * including input/output samples, timestamps, and execution context.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val tracedFlow = flow.withPluginTrace()
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.plugin.trace)
 * @return ApexFlow instance with trace functionality enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPluginTrace(
    loggerName: String = "${ApexFlowConstants.APEXFLOW_NAMESPACE}.plugin.trace"
): ApexFlow<I, O> {
    return withPlugin(ApexTracePlugin(loggerName))
}

/**
 * Extension function: add slow operation detector plugin
 *
 * Automatically detects and reports operations that take longer than a specified threshold.
 * Helps identify performance bottlenecks without manual analysis.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val monitoredFlow = flow.withPluginSlowOperationDetector(Duration.ofSeconds(1))
 * ```
 *
 * @param threshold Duration threshold for slow operations (default: 1 second)
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.plugin.slow-operation)
 * @return ApexFlow instance with slow operation detection enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPluginSlowOperationDetector(
    threshold: Duration = Duration.ofSeconds(1),
    loggerName: String = "${ApexFlowConstants.APEXFLOW_NAMESPACE}.plugin.slow-operation"
): ApexFlow<I, O> {
    return withPlugin(ApexSlowOperationDetectorPlugin(threshold, loggerName))
}

/**
 * Extension function: add throughput monitoring plugin
 *
 * Monitors the number of items processed per unit time, calculating average and peak throughput.
 * Helps understand flow processing capacity and evaluate system performance under high load.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val monitoredFlow = flow.withPluginThroughput()
 * ```
 *
 * @param loggerName SLF4J logger name (default: dev.waylon.apexflow.plugin.throughput)
 * @param samplingInterval Sampling interval for throughput calculation (default: 1 second)
 * @return ApexFlow instance with throughput monitoring enabled
 */
@ApexFlowDsl
fun <I, O> ApexFlow<I, O>.withPluginThroughput(
    loggerName: String = "${ApexFlowConstants.APEXFLOW_NAMESPACE}.plugin.throughput",
    samplingInterval: Duration = Duration.ofSeconds(1)
): ApexFlow<I, O> {
    return withPlugin(ApexThroughputPlugin(loggerName, samplingInterval))
}


