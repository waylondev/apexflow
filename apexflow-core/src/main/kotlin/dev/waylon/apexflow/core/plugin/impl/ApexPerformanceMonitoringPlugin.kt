package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.OperatingSystemMXBean
import java.lang.management.ThreadMXBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory

/**
 * Performance monitoring plugin for ApexFlow
 * This plugin tracks CPU, memory, and thread usage during flow execution
 * and logs the metrics using SLF4J.
 */
class ApexPerformanceMonitoringPlugin(
    private val loggerName: String = "dev.waylon.apexflow.performance"
) : ApexFlowPlugin {

    private val logger = LoggerFactory.getLogger(loggerName)
    private val osBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
    private val memoryBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()
    private val threadBean: ThreadMXBean = ManagementFactory.getThreadMXBean()

    private var startTime: Long = 0
    private var initialCpuTime: Long = 0
    private var initialMemoryUsed: Long = 0
    private var initialThreadCount: Int = 0

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                return input
                    .onStart {
                        startTime = System.currentTimeMillis()
                        initialCpuTime = getCurrentCpuTime()
                        initialMemoryUsed = memoryBean.heapMemoryUsage.used
                        initialThreadCount = threadBean.threadCount

                        logger.info("=== Flow Execution Started ===")
                        logger.info("Start Time: $startTime")
                        logger.info("Initial CPU Time: ${initialCpuTime}ms")
                        logger.info("Initial Heap Memory Used: ${formatBytes(initialMemoryUsed)}")
                        logger.info("Initial Thread Count: $initialThreadCount")
                        logger.info("Available Processors: ${osBean.availableProcessors}")
                        logger.info("System Load Average: ${osBean.systemLoadAverage}")
                    }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onEach { output ->
                        val currentTime = System.currentTimeMillis()
                        val elapsedTime = currentTime - startTime
                        val currentCpuTime = getCurrentCpuTime()
                        val cpuUsed = currentCpuTime - initialCpuTime
                        val currentMemoryUsed = memoryBean.heapMemoryUsage.used
                        val memoryUsed = currentMemoryUsed - initialMemoryUsed
                        val currentThreadCount = threadBean.threadCount
                        val threadCountChange = currentThreadCount - initialThreadCount

                        logger.info("=== Flow Step Completed ===")
                        logger.info("Elapsed Time: ${elapsedTime}ms")
                        logger.info("CPU Time Used: ${cpuUsed}ms")
                        logger.info("Heap Memory Used: ${formatBytes(currentMemoryUsed)} (${formatBytes(memoryUsed)} since start)")
                        logger.info("Thread Count: $currentThreadCount (${threadCountChange} since start)")
                    }
                    .catch { exception ->
                        val currentTime = System.currentTimeMillis()
                        val elapsedTime = currentTime - startTime
                        val currentCpuTime = getCurrentCpuTime()
                        val cpuUsed = currentCpuTime - initialCpuTime
                        val currentMemoryUsed = memoryBean.heapMemoryUsage.used
                        val memoryUsed = currentMemoryUsed - initialMemoryUsed

                        logger.error("=== Flow Execution Failed ===")
                        logger.error("Elapsed Time: ${elapsedTime}ms")
                        logger.error("CPU Time Used: ${cpuUsed}ms")
                        logger.error("Heap Memory Used: ${formatBytes(currentMemoryUsed)} (${formatBytes(memoryUsed)} since start)")
                        logger.error("Exception: ${exception.message}", exception)
                        throw exception
                    }
                    .onCompletion { cause ->
                        val currentTime = System.currentTimeMillis()
                        val elapsedTime = currentTime - startTime
                        val currentCpuTime = getCurrentCpuTime()
                        val cpuUsed = currentCpuTime - initialCpuTime
                        val currentMemoryUsed = memoryBean.heapMemoryUsage.used
                        val memoryUsed = currentMemoryUsed - initialMemoryUsed
                        val finalThreadCount = threadBean.threadCount
                        val threadCountChange = finalThreadCount - initialThreadCount

                        if (cause == null) {
                            logger.info("=== Flow Execution Completed Successfully ===")
                        } else {
                            logger.error("=== Flow Execution Completed with Error ===")
                            logger.error("Error Cause: ${cause.message}", cause)
                        }

                        logger.info("Total Execution Time: ${elapsedTime}ms")
                        logger.info("Total CPU Time Used: ${cpuUsed}ms")
                        logger.info("Heap Memory Max: ${formatBytes(memoryBean.heapMemoryUsage.max)}")
                        logger.info("Heap Memory Used: ${formatBytes(currentMemoryUsed)} (${formatBytes(memoryUsed)} since start)")
                        logger.info("Final Thread Count: $finalThreadCount (${threadCountChange} since start)")
                    }
            }
        }
    }

    private fun getCurrentCpuTime(): Long {
        return threadBean.currentThreadCpuTime / 1_000_000 // Convert nanoseconds to milliseconds
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024
        if (kb < 1024) return "$kb KB"
        val mb = kb / 1024
        if (mb < 1024) return "$mb MB"
        val gb = mb / 1024
        return "$gb GB"
    }
}