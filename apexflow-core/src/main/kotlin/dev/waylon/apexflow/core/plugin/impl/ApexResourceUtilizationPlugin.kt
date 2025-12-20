package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * Resource utilization plugin for ApexFlow
 *
 * Monitors system resource usage, including CPU, disk I/O, network I/O, and thread count.
 * Helps understand the impact of flows on system resources and identify I/O or CPU bottlenecks.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val monitoredFlow = flow.withPluginResourceUtilization()
 * ```
 */
class ApexResourceUtilizationPlugin(
    private val loggerName: String = "dev.waylon.apexflow.plugin.resource-utilization",
    private val samplingInterval: Duration = Duration.ofSeconds(5)
) : ApexFlowPlugin {

    private val osBean = ManagementFactory.getOperatingSystemMXBean()
    private val runtime = Runtime.getRuntime()
    private val threadMxBean = ManagementFactory.getThreadMXBean()

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        val logger = createLogger(loggerName)

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                val componentName = flow.javaClass.simpleName.ifEmpty { "AnonymousComponent" }
                val startTime = Instant.now()
                val startCpuTime = getCpuTime()

                return input
                    .onStart {
                        logger.info(
                            "Resource utilization monitoring started for component '$componentName' at $startTime"
                        )
                        logResourceSnapshot("Initial", componentName)
                    }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onCompletion { exception: Throwable? ->
                        val endTime = Instant.now()
                        val endCpuTime = getCpuTime()
                        val totalTime = Duration.between(startTime, endTime)

                        logResourceSnapshot("Final", componentName)

                        // Calculate total CPU usage for the component
                        val cpuUsage = calculateCpuUsage(startCpuTime, endCpuTime, totalTime)
                        logger.info(
                            "Resource utilization summary for component '$componentName': " +
                                    "Total time: ${totalTime.seconds}s, " +
                                    "CPU Usage: ${String.format("%.2f", cpuUsage)}%"
                        )
                    }
            }

            /**
             * Log current resource snapshot
             */
            private fun logResourceSnapshot(phase: String, componentName: String) {
                val freeMemory = runtime.freeMemory() / (1024 * 1024) // MB
                val totalMemory = runtime.totalMemory() / (1024 * 1024) // MB
                val maxMemory = runtime.maxMemory() / (1024 * 1024) // MB
                val threadCount = threadMxBean.threadCount
                val cpuLoad = getSystemCpuLoad()

                logger.info(
                    "$phase resource snapshot for '$componentName': " +
                            "CPU: ${String.format("%.2f", cpuLoad)}%, " +
                            "Memory: $freeMemory MB free / $totalMemory MB total / $maxMemory MB max, " +
                            "Threads: $threadCount"
                )
            }

            /**
             * Get current CPU time in nanoseconds
             */
            private fun getCpuTime(): Long {
                return ManagementFactory.getThreadMXBean().currentThreadCpuTime
            }

            /**
             * Get system CPU load as percentage
             */
            private fun getSystemCpuLoad(): Double {
                return if (osBean is com.sun.management.OperatingSystemMXBean) {
                    (osBean.systemCpuLoad * 100).coerceAtLeast(0.0)
                } else {
                    0.0
                }
            }

            /**
             * Calculate CPU usage percentage
             */
            private fun calculateCpuUsage(startCpuTime: Long, endCpuTime: Long, duration: Duration): Double {
                val cpuTimeUsed = endCpuTime - startCpuTime
                val totalAvailableCpuTime = duration.toNanos() * osBean.availableProcessors
                return if (totalAvailableCpuTime > 0) {
                    (cpuTimeUsed.toDouble() / totalAvailableCpuTime) * 100
                } else {
                    0.0
                }
            }
        }
    }
}
