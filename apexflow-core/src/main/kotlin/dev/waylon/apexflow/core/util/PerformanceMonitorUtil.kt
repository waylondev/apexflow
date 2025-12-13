package dev.waylon.apexflow.core.util

import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage
import java.text.DecimalFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.Logger

/**
 * Performance Monitoring Utility
 *
 * Provides methods to monitor and measure the performance of code execution
 * including execution time, memory usage, and garbage collection statistics.
 */
object PerformanceMonitorUtil {

    private val decimalFormat = DecimalFormat("#,##0.00")
    private val memoryMXBean = ManagementFactory.getMemoryMXBean()
    private val gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans()

    /**
     * Executes the given block with performance monitoring
     *
     * @param logger Logger to use for logging performance data
     * @param pageCount Estimated or actual page count for calculation
     * @param block Block of code to execute and monitor
     */
    suspend fun <T> withPerformanceMonitoring(
        logger: Logger,
        pageCount: Int = 0,
        block: suspend () -> T
    ): T {
        // Record start time
        val startTime = System.currentTimeMillis()
        val startMemory = getMemoryUsage()
        val startGcStats = getGcStats()

        if (logger.isInfoEnabled) {
            logger.info("🚀 Starting performance monitoring")
            logger.info("📊 Monitoring metrics: execution time, memory usage, GC activity")
            if (pageCount > 0) {
                logger.info("📄 Estimated page count: $pageCount")
            }
        }

        // Launch background memory monitoring coroutine
        val monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            var counter = 0
            while (isActive) {
                delay(1000)
                counter++
                if (counter % 5 == 0) { // Log every 5 seconds
                    val memoryUsage = getMemoryUsage()
                    if (logger.isDebugEnabled) {
                        logger.debug(
                            "📊 Memory usage: Used {} MB, Max {} MB",
                            decimalFormat.format(memoryUsage.used.toDouble() / (1024 * 1024)),
                            decimalFormat.format(memoryUsage.max.toDouble() / (1024 * 1024))
                        )
                    }
                }
            }
        }

        try {
            // Execute the block
            val result = block()

            // Record end time
            val endTime = System.currentTimeMillis()
            val endMemory = getMemoryUsage()
            val endGcStats = getGcStats()

            // Calculate performance metrics
            val executionTime = endTime - startTime
            val memoryUsed = endMemory.used - startMemory.used
            val gcTime = calculateGcTimeDifference(startGcStats, endGcStats)
            val gcCount = calculateGcCountDifference(startGcStats, endGcStats)

            // Log performance summary
            logPerformanceSummary(
                logger = logger,
                executionTime = executionTime,
                memoryUsed = memoryUsed,
                gcTime = gcTime,
                gcCount = gcCount,
                pageCount = pageCount
            )

            return result
        } finally {
            // Cancel background monitoring job
            monitoringJob.cancel()
        }
    }

    /**
     * Get current memory usage
     */
    private fun getMemoryUsage(): MemoryUsage {
        return memoryMXBean.heapMemoryUsage
    }

    /**
     * Get current garbage collection statistics
     */
    private fun getGcStats(): List<GcStats> {
        return gcMXBeans.map {
            GcStats(
                name = it.name,
                collectionCount = it.collectionCount,
                collectionTime = it.collectionTime
            )
        }
    }

    /**
     * Calculate garbage collection time difference between two states
     */
    private fun calculateGcTimeDifference(startStats: List<GcStats>, endStats: List<GcStats>): Long {
        var totalDifference = 0L

        for (startStat in startStats) {
            val endStat = endStats.find { it.name == startStat.name }
            if (endStat != null) {
                totalDifference += (endStat.collectionTime - startStat.collectionTime)
            }
        }

        return totalDifference
    }

    /**
     * Calculate garbage collection count difference between two states
     */
    private fun calculateGcCountDifference(startStats: List<GcStats>, endStats: List<GcStats>): Long {
        var totalDifference = 0L

        for (startStat in startStats) {
            val endStat = endStats.find { it.name == startStat.name }
            if (endStat != null) {
                totalDifference += (endStat.collectionCount - startStat.collectionCount)
            }
        }

        return totalDifference
    }

    /**
     * Log performance summary
     */
    private fun logPerformanceSummary(
        logger: Logger,
        executionTime: Long,
        memoryUsed: Long,
        gcTime: Long,
        gcCount: Long,
        pageCount: Int
    ) {
        if (logger.isInfoEnabled) {
            logger.info("\n📊 Performance Summary:")
            logger.info("=".repeat(60))

            // Execution time
            val seconds = executionTime.toDouble() / 1000
            logger.info("⏱️  Execution Time: ${decimalFormat.format(seconds)} seconds ($executionTime ms)")

            // Pages per second if page count is provided
            if (pageCount > 0) {
                val pagesPerSecond = pageCount / seconds
                logger.info("📄 Pages Processed: $pageCount")
                logger.info("⚡ Pages per Second: ${decimalFormat.format(pagesPerSecond)}")
            }

            // Memory usage
            val memoryUsedMB = memoryUsed.toDouble() / (1024 * 1024)
            logger.info("💾 Memory Used: ${decimalFormat.format(memoryUsedMB)} MB")

            // GC statistics
            logger.info("🗑️  Garbage Collection: $gcCount collections, ${decimalFormat.format(gcTime.toDouble() / 1000)} seconds")

            // Final summary
            logger.info("=".repeat(60))
            logger.info("✅ Performance monitoring completed")
        }
    }

    /**
     * Garbage Collection Statistics
     */
    private data class GcStats(
        val name: String,
        val collectionCount: Long,
        val collectionTime: Long
    )
}