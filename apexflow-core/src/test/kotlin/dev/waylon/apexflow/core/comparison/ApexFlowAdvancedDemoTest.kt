package dev.waylon.apexflow.core.comparison

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.withPerformanceMonitoring
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sin
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Advanced ApexFlow demo showcasing real-world advantages
 *
 * Scenario: Real-time data processing pipeline with:
 * 1. Multiple data sources (fast, medium, slow)
 * 2. Complex transformation logic
 * 3. Error handling and retry mechanisms
 * 4. Rate limiting and backpressure handling
 * 5. Performance monitoring and observability
 */
class ApexFlowAdvancedDemoTest {

    // Test data classes
    data class DataPoint(val source: String, val value: Int, val timestamp: Long)
    data class ProcessedData(val source: String, val value: Int, val processedValue: Double, val status: String)
    data class AggregatedResult(
        val totalProcessed: Int,
        val successCount: Int,
        val errorCount: Int,
        val avgProcessingTime: Double
    )

    // Simulate different data sources with varying speeds
    private val fastSourceDelay = 100L
    private val mediumSourceDelay = 300L
    private val slowSourceDelay = 800L

    private val processedCount = AtomicInteger(0)
    private val errorCount = AtomicInteger(0)

    /**
     * Traditional implementation - becomes complex and hard to maintain
     */
    private suspend fun traditionalPipeline(dataPoints: List<DataPoint>): AggregatedResult {
        val results = mutableListOf<ProcessedData>()
        val startTime = System.currentTimeMillis()
        var successCount = 0
        var errorCount = 0

        // Complex nested logic
        dataPoints.forEach { dataPoint ->
            try {
                // Rate limiting per source type
                when (dataPoint.source) {
                    "fast" -> delay(fastSourceDelay)
                    "medium" -> delay(mediumSourceDelay)
                    "slow" -> delay(slowSourceDelay)
                }

                // Complex transformation with potential errors
                val processedValue = if (dataPoint.value % 10 == 0) {
                    throw IllegalArgumentException("Invalid value: ${dataPoint.value}")
                } else {
                    dataPoint.value * 1.5 + sin(dataPoint.value.toDouble())
                }

                val result = ProcessedData(
                    source = dataPoint.source,
                    value = dataPoint.value,
                    processedValue = processedValue,
                    status = "SUCCESS"
                )
                results.add(result)
                successCount++
            } catch (e: Exception) {
                errorCount++
                // Error handling becomes scattered
                println("Error processing ${dataPoint.source}: ${e.message}")
            }
        }

        val totalTime = System.currentTimeMillis() - startTime
        return AggregatedResult(
            totalProcessed = results.size,
            successCount = successCount,
            errorCount = errorCount,
            avgProcessingTime = totalTime.toDouble() / results.size
        )
    }

    /**
     * ApexFlow implementation - showcases real advantages
     */
    private fun createAdvancedApexFlow(): ApexFlow<DataPoint, ProcessedData> {
        // 1. Source-specific rate limiting component
        val rateLimitComponent = apexFlow<DataPoint, DataPoint> {
            map { dataPoint ->
                when (dataPoint.source) {
                    "fast" -> delay(fastSourceDelay)
                    "medium" -> delay(mediumSourceDelay)
                    "slow" -> delay(slowSourceDelay)
                }
                dataPoint
            }
        }

        // 2. Complex transformation component with error handling
        val transformationComponent = apexFlow<DataPoint, ProcessedData> {
            map { dataPoint ->
                // Complex business logic
                val processedValue = if (dataPoint.value % 10 == 0) {
                    throw IllegalArgumentException("Invalid value: ${dataPoint.value}")
                } else {
                    dataPoint.value * 1.5 + sin(dataPoint.value.toDouble())
                }

                ProcessedData(
                    source = dataPoint.source,
                    value = dataPoint.value,
                    processedValue = processedValue,
                    status = "SUCCESS"
                )
            }
        }

        // 3. Error handling and retry component using existing DSL
        val errorHandlingComponent = apexFlow<DataPoint, ProcessedData> {
            map { dataPoint ->
                var lastException: Exception? = null
                for (attempt in 1..3) {
                    try {
                        // Execute the combined flow for this data point
                        val result = (rateLimitComponent + transformationComponent)
                            .transform(flowOf(dataPoint))
                            .toList()
                            .first()
                        return@map result
                    } catch (e: Exception) {
                        lastException = e
                        if (attempt < 3 && e is IllegalArgumentException) {
                            // Retry on specific exceptions
                            continue
                        } else {
                            throw e
                        }
                    }
                }
                throw lastException ?: IllegalStateException("Retry logic failed")
            }
        }

        // 4. Monitoring and observability component
        val monitoringComponent = apexFlow<ProcessedData, ProcessedData> {
            map { result ->
                processedCount.incrementAndGet()
                result
            }
        }

        // 🌟 Core Advantage: Declarative composition with built-in features
        return errorHandlingComponent + monitoringComponent
    }

    /**
     * Scenario 1: Real-time streaming with backpressure
     */
    @Test
    fun `real-time streaming with backpressure`() = runBlocking {
        // Simulate real-time data stream
        val dataStream = flow {
            repeat(100) { index ->
                val source = when (index % 3) {
                    0 -> "fast"
                    1 -> "medium"
                    else -> "slow"
                }
                emit(DataPoint(source, index, System.currentTimeMillis()))
                delay(50) // Fast data generation
            }
        }

        val apexFlow = createAdvancedApexFlow()
            .withPerformanceMonitoring("real-time-pipeline")

        val startTime = System.currentTimeMillis()
        val results = dataStream
            .buffer(10) // Handle backpressure using standard Flow API
            .let { stream -> apexFlow.transform(stream) }
            .take(20) // Limit for demo
            .toList()

        val duration = System.currentTimeMillis() - startTime
        println("Real-time streaming completed in ${duration}ms, processed ${results.size} items")
        assertTrue(results.isNotEmpty())
    }

    /**
     * Scenario 2: Complex data transformation with error recovery
     */
    @Test
    fun `complex transformation with error recovery`() = runBlocking {
        val testData = listOf(
            DataPoint("fast", 1, System.currentTimeMillis()),
            DataPoint("medium", 10, System.currentTimeMillis()), // This will cause error
            DataPoint("slow", 2, System.currentTimeMillis()),
            DataPoint("fast", 20, System.currentTimeMillis()), // This will cause error
            DataPoint("medium", 3, System.currentTimeMillis())
        )

        val apexFlow = createAdvancedApexFlow()
            .withPerformanceMonitoring("error-recovery-pipeline")

        val results = testData.asFlow()
            .let { stream -> apexFlow.transform(stream) }
            .catch { exception ->
                // Global error handling
                errorCount.incrementAndGet()
                println("Global error caught: ${exception.message}")
            }
            .toList()

        println("Error recovery test: Success=${results.size}, Errors=${errorCount.get()}")
        assertTrue(results.isNotEmpty())
        assertTrue(errorCount.get() > 0)
    }

    /**
     * Scenario 3: Performance comparison with traditional approach
     */
    @Test
    fun `performance comparison advanced scenario`() = runBlocking {
        val testData = List(50) { index ->
            val source = when (index % 3) {
                0 -> "fast"
                1 -> "medium"
                else -> "slow"
            }
            DataPoint(source, index, System.currentTimeMillis())
        }

        // Traditional approach
        val traditionalTime = measureTimeMillis {
            val result = traditionalPipeline(testData)
            println("Traditional: ${result.totalProcessed} items in ${result.avgProcessingTime}ms avg")
        }

        // Reset counters
        processedCount.set(0)
        errorCount.set(0)

        // ApexFlow approach
        val apexFlow = createAdvancedApexFlow()
            .withPerformanceMonitoring("performance-comparison")

        val apexFlowTime = measureTimeMillis {
            val results = testData.asFlow()
                .let { stream -> apexFlow.transform(stream) }
                .toList()
            println("ApexFlow: ${results.size} items processed")
        }

        val efficiency = traditionalTime.toDouble() / apexFlowTime
        println("Advanced Performance: Traditional=$traditionalTime ms, ApexFlow=$apexFlowTime ms, Efficiency=$efficiency x")

        // ApexFlow should be more efficient in complex scenarios
        assertTrue(efficiency > 0.5) // At least 50% as efficient
    }

    /**
     * Scenario 4: Dynamic pipeline modification
     */
    @Test
    fun `dynamic pipeline modification`() = runBlocking {
        val baseFlow = createAdvancedApexFlow()

        // Dynamically add filtering based on runtime conditions
        val shouldFilterEven = System.currentTimeMillis() % 2 == 0L

        val filteredFlow = apexFlow<ProcessedData, ProcessedData> {
            filter { processedData ->
                if (shouldFilterEven) {
                    processedData.value % 2 == 0 // Even values only
                } else {
                    processedData.value % 2 != 0 // Odd values only
                }
            }
        }

        val testData = List(10) { index ->
            DataPoint("dynamic", index, System.currentTimeMillis())
        }

        val results = testData.asFlow()
            .let { stream -> (baseFlow + filteredFlow).transform(stream) }
            .toList()

        println("Dynamic pipeline processed ${results.size} items")
        assertTrue(results.isNotEmpty())
    }
}