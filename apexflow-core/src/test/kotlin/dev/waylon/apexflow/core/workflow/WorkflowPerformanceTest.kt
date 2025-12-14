package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Performance tests for workflow engine
 */
class WorkflowPerformanceTest {

    /**
     * Test performance with large dataset
     *
     * This test measures the time it takes to process 10,000 items
     * It doesn't have a strict time limit, but serves as a baseline for performance monitoring
     */
    @Test
    fun testPerformanceWithLargeDataset() = runBlocking {
        // Number of items to process
        val itemCount = 10000

        // Mock reader that emits a large number of items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                repeat(itemCount) {
                    emit(it)
                }
            }
        }

        // Mock processor that doubles the input
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> = input
        }

        // Mock writer that collects items
        val mockWriter = object : WorkflowWriter<Int> {
            override suspend fun write(data: Flow<Int>) {
                // Just collect the data without doing anything
                data.collect { }
            }
        }

        // Create workflow engine
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)

        // Start the engine and measure time
        val startTime = System.currentTimeMillis()
        engine.startAsync()
        val endTime = System.currentTimeMillis()

        // Calculate and print execution time
        val executionTime = endTime - startTime
        println("Processing $itemCount items took $executionTime ms")

        // This test doesn't assert a specific time, but serves as a performance baseline
        // In a real scenario, you might add assertions based on expected performance characteristics
    }
}