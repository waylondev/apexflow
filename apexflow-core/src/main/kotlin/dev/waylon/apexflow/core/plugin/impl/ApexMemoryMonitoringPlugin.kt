package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion

/**
 * Memory monitoring plugin for ApexFlow
 *
 * Monitors memory usage, including memory peaks, garbage collection frequency,
 * and object creation statistics. Helps detect memory leaks or excessive usage.
 *
 * Usage Example:
 * ```kotlin
 * val flow = apexFlow { ... }
 * val memoryMonitoredFlow = flow.withPluginMemoryMonitoring()
 * ```
 */
class ApexMemoryMonitoringPlugin(
    private val loggerName: String = "dev.waylon.apexflow.plugin.memory-monitoring",
    private val samplingInterval: Duration = Duration.ofSeconds(2)
) : ApexFlowPlugin {

    private val memoryMXBean = ManagementFactory.getMemoryMXBean()
    private val gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans()

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        val logger = createLogger(loggerName)

        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                val componentName = flow.javaClass.simpleName.ifEmpty { "AnonymousComponent" }
                val startTime = Instant.now()

                // Record initial memory stats
                val initialHeapMemory = getHeapMemoryUsage()
                val initialNonHeapMemory = getNonHeapMemoryUsage()
                val initialGcCount = getGcCount()

                return input
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onCompletion { exception: Throwable? ->
                        val endTime = Instant.now()
                        val executionTime = Duration.between(startTime, endTime)

                        // Record final memory stats
                        val finalHeapMemory = getHeapMemoryUsage()
                        val finalNonHeapMemory = getNonHeapMemoryUsage()
                        val finalGcCount = getGcCount()

                        // Calculate memory and GC statistics
                        val heapMemoryIncrease = finalHeapMemory - initialHeapMemory
                        val nonHeapMemoryIncrease = finalNonHeapMemory - initialNonHeapMemory
                        val gcCountIncrease = finalGcCount - initialGcCount

                        // Log memory summary
                        logger.info(buildString {
                            appendLine("Memory Monitoring Summary for '$componentName':")
                            appendLine("- Execution Time: $executionTime")
                            appendLine(
                                "- Heap Memory: ${formatBytes(initialHeapMemory)} -> ${
                                    formatBytes(
                                        finalHeapMemory
                                    )
                                } (+${formatBytes(heapMemoryIncrease)})"
                            )
                            appendLine(
                                "- Non-Heap Memory: ${formatBytes(initialNonHeapMemory)} -> ${
                                    formatBytes(
                                        finalNonHeapMemory
                                    )
                                } (+${formatBytes(nonHeapMemoryIncrease)})"
                            )
                            appendLine("- Garbage Collections: $initialGcCount -> $finalGcCount (+$gcCountIncrease)")
                            appendLine("- Final Heap Usage: ${memoryMXBean.heapMemoryUsage.used} / ${memoryMXBean.heapMemoryUsage.max} (${(memoryMXBean.heapMemoryUsage.used * 100 / memoryMXBean.heapMemoryUsage.max)}%)")
                        })
                    }
            }
        }
    }

    /**
     * Get current heap memory usage in bytes
     */
    private fun getHeapMemoryUsage(): Long {
        return memoryMXBean.heapMemoryUsage.used
    }

    /**
     * Get current non-heap memory usage in bytes
     */
    private fun getNonHeapMemoryUsage(): Long {
        return memoryMXBean.nonHeapMemoryUsage.used
    }

    /**
     * Get total garbage collection count
     */
    private fun getGcCount(): Long {
        return gcMXBeans.sumOf { it.collectionCount }
    }

    /**
     * Format bytes to human-readable string
     */
    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val z = (63 - bytes.countLeadingZeroBits()) / 10
        return String.format("%.1f %cB", bytes.toDouble() / (1L shl (z * 10)), " KMGTPE"[z])
    }
}
