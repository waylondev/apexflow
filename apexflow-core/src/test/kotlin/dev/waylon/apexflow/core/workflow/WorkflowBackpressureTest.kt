package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Backpressure tests for workflow engine
 */
class WorkflowBackpressureTest {

    /**
     * Test backpressure handling with slow writer
     *
     * This test verifies that the workflow engine can handle backpressure when the writer is slow
     */
    @Test
    fun testBackpressureWithSlowWriter() = runBlocking {
        // Number of items to process
        val itemCount = 1000

        // Mock reader that emits items quickly
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                repeat(itemCount) {
                    emit(it)
                }
            }
        }

        // Mock processor that processes items quickly
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { it + 1 }
            }
        }

        // Mock writer that is slow (simulates IO bottleneck)
        val collectedItems = mutableListOf<Int>()
        val mockWriter = object : WorkflowWriter<Int> {
            override suspend fun write(data: Flow<Int>) {
                data.collect {
                    // Simulate slow writing (1ms per item)
                    delay(1)
                    collectedItems.add(it)
                }
            }
        }

        // Create workflow engine with small buffers (to trigger backpressure)
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)
        engine.configure(
            WorkflowConfig(
                readBufferSize = 10,
                processBufferSize = 10
            )
        )

        // Measure execution time
        val startTime = System.currentTimeMillis()

        // Start workflow
        engine.startAsync()

        // Calculate execution time
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // Print performance metrics
        println("Backpressure test: Processed $itemCount items in $executionTime ms")

        // Verify results
        assertEquals(itemCount, collectedItems.size)
    }

    /**
     * Test backpressure with fast writer
     *
     * This test verifies that the workflow engine can handle fast writers efficiently
     */
    @Test
    fun testBackpressureWithFastWriter() = runBlocking {
        // Number of items to process
        val itemCount = 10000

        // Mock reader that emits items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                repeat(itemCount) {
                    emit(it)
                }
            }
        }

        // Mock processor that processes items
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { it + 1 }
            }
        }

        // Mock writer that is fast
        val collectedItems = mutableListOf<Int>()
        val mockWriter = object : WorkflowWriter<Int> {
            override suspend fun write(data: Flow<Int>) {
                data.collect {
                    collectedItems.add(it)
                }
            }
        }

        // Create workflow engine with large buffers
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)
        engine.configure(
            WorkflowConfig(
                readBufferSize = 1000,
                processBufferSize = 1000
            )
        )

        // Measure execution time
        val startTime = System.currentTimeMillis()

        // Start workflow
        engine.startAsync()

        // Calculate execution time
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // Print performance metrics
        println("Fast writer test: Processed $itemCount items in $executionTime ms")

        // Verify results
        assertEquals(itemCount, collectedItems.size)
    }

    /**
     * Test backpressure with varying processing times
     *
     * This test verifies that the workflow engine can handle varying processing times
     */
    @Test
    fun testBackpressureWithVaryingProcessingTimes() = runBlocking {
        // Number of items to process
        val itemCount = 1000

        // Mock reader that emits items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                repeat(itemCount) {
                    emit(it)
                }
            }
        }

        // Mock processor with varying processing times
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.onEach {
                    // Varying processing time (0-5ms per item)
                    if (it % 10 == 0) {
                        delay(5) // Simulate occasional slow processing
                    }
                }.map { it + 1 }
            }
        }

        // Mock writer that collects items
        val collectedItems = mutableListOf<Int>()
        val mockWriter = object : WorkflowWriter<Int> {
            override suspend fun write(data: Flow<Int>) {
                data.collect {
                    collectedItems.add(it)
                }
            }
        }

        // Create workflow engine
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)

        // Start workflow
        engine.startAsync()

        // Verify results
        assertEquals(itemCount, collectedItems.size)
    }
}
