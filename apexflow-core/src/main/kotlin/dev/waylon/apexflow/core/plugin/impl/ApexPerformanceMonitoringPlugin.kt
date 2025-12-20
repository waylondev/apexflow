package dev.waylon.apexflow.core.plugin.impl

import com.sun.management.OperatingSystemMXBean
import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import dev.waylon.apexflow.core.util.createLogger
import java.lang.management.ManagementFactory
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.slf4j.Logger

/**
 * Performance monitoring plugin for ApexFlow
 *
 * Enhanced plugin that tracks comprehensive system metrics including:
 * - CPU usage (process and system-wide)
 * - Memory usage (heap, non-heap, pools)
 * - Thread states and contention
 * - Garbage collection statistics
 * - Memory pool utilization
 *
 * Uses more effective JMX APIs and reduces logging overhead
 */
class ApexPerformanceMonitoringPlugin(
    private val loggerName: String = "dev.waylon.apexflow.plugin.performance",
    private val samplingIntervalMs: Long = 5000,
    private val enableDetailedMetrics: Boolean = false
) : ApexFlowPlugin {

    private val logger: Logger = createLogger(loggerName)
    private val osBean: OperatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
    private val memoryBean = ManagementFactory.getMemoryMXBean()
    private val threadBean = ManagementFactory.getThreadMXBean()
    private val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
    private val memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans()

    private var startTime: Long = 0
    private var lastSampleTime: Long = 0
    private var initialCpuTime: Long = 0
    private var initialMemoryUsed: Long = 0
    private var initialThreadCount: Int = 0
    private var initialGcCount: Long = 0
    private var initialGcTime: Long = 0

    override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
        return object : ApexFlow<I, O> {
            override fun transform(input: Flow<I>): Flow<O> {
                return input
                    .onStart {
                        initializeMetrics()
                        logStartMetrics()
                    }
                    .let { originalFlow -> flow.transform(originalFlow) }
                    .onEach { _ ->
                        val currentTime = System.nanoTime()
                        if (currentTime - lastSampleTime >= samplingIntervalMs * 1_000_000) {
                            logSampleMetrics(currentTime)
                            lastSampleTime = currentTime
                        }
                    }
                    .catch { exception ->
                        logErrorMetrics(exception)
                        throw exception
                    }
                    .onCompletion { cause ->
                        logCompletionMetrics(cause)
                    }
            }
        }
    }

    private fun initializeMetrics() {
        startTime = System.nanoTime()
        lastSampleTime = startTime
        initialCpuTime = getProcessCpuTime()
        initialMemoryUsed = memoryBean.heapMemoryUsage.used
        initialThreadCount = threadBean.threadCount
        initialGcCount = getTotalGcCount()
        initialGcTime = getTotalGcTime()
    }

    private fun logStartMetrics() {
        logger.info("=== Flow Execution Started ===")
        logger.info("Start Time: $startTime")
        logger.info("Available Processors: ${osBean.availableProcessors}")
        logger.info("System Load Average: ${formatLoadAverage(osBean.systemLoadAverage)}")

        if (enableDetailedMetrics) {
            logDetailedMemoryMetrics()
            logThreadStateMetrics()
        }
    }

    private fun logSampleMetrics(currentTime: Long) {
        val elapsedTime = (currentTime - startTime).toDuration(DurationUnit.NANOSECONDS)

        logger.debug("=== Flow Sample ===")
        logger.debug("Elapsed Time: $elapsedTime")

        if (enableDetailedMetrics) {
            logCpuMetrics(elapsedTime)
            logMemoryMetrics()
            logThreadMetrics()
            logGcMetrics()
        }
    }

    private fun logErrorMetrics(exception: Throwable) {
        val currentTime = System.nanoTime()
        val elapsedTime = (currentTime - startTime).toDuration(DurationUnit.NANOSECONDS)
        val cpuUsed = (getProcessCpuTime() - initialCpuTime).toDuration(DurationUnit.NANOSECONDS)

        logger.error("=== Flow Execution Failed ===")
        logger.error("Elapsed Time: $elapsedTime")
        logger.error("CPU Time Used: $cpuUsed")
        logger.error("Exception: ${exception.message}", exception)
    }

    private fun logCompletionMetrics(cause: Throwable?) {
        val currentTime = System.nanoTime()
        val elapsedTime = (currentTime - startTime).toDuration(DurationUnit.NANOSECONDS)
        val cpuUsed = (getProcessCpuTime() - initialCpuTime).toDuration(DurationUnit.NANOSECONDS)
        val memoryUsed = memoryBean.heapMemoryUsage.used - initialMemoryUsed
        val threadCountChange = threadBean.threadCount - initialThreadCount
        val gcCountChange = getTotalGcCount() - initialGcCount
        val gcTimeChange = getTotalGcTime() - initialGcTime

        if (cause == null) {
            logger.info("=== Flow Execution Completed Successfully ===")
        } else {
            logger.error("=== Flow Execution Completed with Error ===")
            logger.error("Error Cause: ${cause.message}", cause)
        }

        logger.info("Total Execution Time: $elapsedTime")
        logger.info("Total CPU Time Used: $cpuUsed")
        logger.info("Heap Memory Used: ${formatBytes(memoryBean.heapMemoryUsage.used)} (${formatBytes(memoryUsed)} since start)")
        logger.info("Thread Count: ${threadBean.threadCount} (${threadCountChange} since start)")
        logger.info("GC Count: ${getTotalGcCount()} (${gcCountChange} since start)")
        logger.info("GC Time: ${getTotalGcTime()}ms (${gcTimeChange}ms since start)")
    }

    private fun logCpuMetrics(elapsedTime: Duration) {
        val cpuUsed = getProcessCpuTime() - initialCpuTime
        val cpuUsagePercent =
            if (elapsedTime.inWholeNanoseconds > 0) (cpuUsed.toDouble() / elapsedTime.inWholeNanoseconds) * 100 else 0.0
        val processCpuLoad = osBean.processCpuLoad * 100
        val systemCpuLoad = osBean.cpuLoad * 100

        logger.debug(
            "CPU Usage: ${cpuUsagePercent.format(2)}% (process), ${processCpuLoad.format(2)}% (system), ${
                systemCpuLoad.format(
                    2
                )
            }% (total)"
        )
    }

    private fun logMemoryMetrics() {
        val heapUsage = memoryBean.heapMemoryUsage
        val nonHeapUsage = memoryBean.nonHeapMemoryUsage

        logger.debug(
            "Heap Memory: ${formatBytes(heapUsage.used)}/${formatBytes(heapUsage.max)} (${
                (heapUsage.used.toDouble() / heapUsage.max * 100).format(
                    2
                )
            }%)"
        )
        logger.debug("Non-Heap Memory: ${formatBytes(nonHeapUsage.used)}/${formatBytes(nonHeapUsage.max)}")

        if (enableDetailedMetrics) {
            logDetailedMemoryMetrics()
        }
    }

    private fun logThreadMetrics() {
        val threadCount = threadBean.threadCount
        val daemonThreadCount = threadBean.daemonThreadCount
        val peakThreadCount = threadBean.peakThreadCount

        logger.debug("Threads: $threadCount total, $daemonThreadCount daemon, $peakThreadCount peak")

        if (enableDetailedMetrics) {
            logThreadStateMetrics()
        }
    }

    private fun logGcMetrics() {
        val gcCount = getTotalGcCount()
        val gcTime = getTotalGcTime()
        val gcCountChange = gcCount - initialGcCount
        gcTime - initialGcTime

        logger.debug("GC: $gcCount collections, $gcTime ms total (${gcCountChange} since start)")
    }

    private fun logDetailedMemoryMetrics() {
        memoryPoolBeans.forEach { pool ->
            val usage = pool.usage
            if (usage.max > 0) {
                val usagePercent = (usage.used.toDouble() / usage.max) * 100
                logger.debug(
                    "${pool.name}: ${formatBytes(usage.used)}/${formatBytes(usage.max)} (${
                        usagePercent.format(
                            2
                        )
                    }%)"
                )
            }
        }
    }

    private fun logThreadStateMetrics() {
        val threadInfos = threadBean.getThreadInfo(threadBean.allThreadIds)
        val stateCounts = threadInfos.groupBy { it?.threadState }

        stateCounts.forEach { (state, threads) ->
            if (state != null && threads.isNotEmpty()) {
                logger.debug("Thread State {}: {} threads", state, threads.size)
            }
        }
    }

    private fun getProcessCpuTime(): Long {
        return osBean.processCpuTime / 1_000_000
    }

    private fun getTotalGcCount(): Long {
        return gcBeans.sumOf { it.collectionCount }
    }

    private fun getTotalGcTime(): Long {
        return gcBeans.sumOf { it.collectionTime }
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

    private fun formatLoadAverage(load: Double): String {
        return if (load >= 0) load.format(2) else "N/A"
    }

    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
}