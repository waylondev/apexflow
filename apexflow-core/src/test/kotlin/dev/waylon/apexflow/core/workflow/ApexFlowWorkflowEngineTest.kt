package dev.waylon.apexflow.core.workflow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Test for ApexFlowWorkflowEngine
 *
 * Verifies core workflow engine capabilities:
 * 1. Complete workflow flow: Reader → Processor → Writer
 * 2. Parallel execution with Flow operators
 * 3. Configuration handling
 * 4. Error handling
 * 5. Basic functionality validation
 */
class ApexFlowWorkflowEngineTest {

    // Mock WorkflowReader implementation for testing
    private class MockReader(private val data: List<String>) : WorkflowReader<String> {
        override fun read(): Flow<String> = data.asFlow()
    }

    // Mock WorkflowProcessor implementation for testing
    private class MockProcessor : WorkflowProcessor<String, Int> {
        override fun process(input: Flow<String>): Flow<Int> {
            return input.map { it.length }
        }
    }

    // Mock WorkflowWriter implementation for testing
    private class MockWriter(val results: MutableList<Int> = mutableListOf()) : WorkflowWriter<Int> {
        override suspend fun write(data: Flow<Int>) {
            data.collect { results.add(it) }
        }
    }

    /**
     * Test basic workflow execution
     * Verifies the complete flow: Reader → Processor → Writer
     */
    @Test
    fun testBasicWorkflowExecution() = runBlocking {
        // Given
        val testData = listOf("apple", "banana", "cherry", "date")
        val expectedResults = listOf(5, 6, 6, 4)

        val reader = MockReader(testData)
        val processor = MockProcessor()
        val writer = MockWriter()

        val engine = ApexFlowWorkflowEngine(reader, processor, writer)

        // When
        engine.startAsync()

        // Then
        assertEquals(expectedResults, writer.results)
    }

    /**
     * Test workflow with custom configuration
     * Verifies that configuration is properly applied
     */
    @Test
    fun testWorkflowWithCustomConfiguration() = runBlocking {
        // Given
        val testData = listOf("test1", "test2", "test3")

        val reader = MockReader(testData)
        val processor = MockProcessor()
        val writer = MockWriter()

        val engine = ApexFlowWorkflowEngine(reader, processor, writer)
        val customConfig = WorkflowConfig(
            readBufferSize = 50,
            processBufferSize = 75,
            errorHandler = { throw it }
        )

        // When
        engine.configure(customConfig)
        engine.startAsync()

        // Then
        assertEquals(listOf(5, 5, 5), writer.results)
    }

    /**
     * Test workflow with error handling
     * Verifies that errors are properly handled
     */
    @Test
    fun testWorkflowWithErrorHandling() = runBlocking {
        // Given
        val testData = listOf("valid", "invalid-data", "another-valid")

        val reader = MockReader(testData)

        // Processor that throws exception for "invalid-data"
        val failingProcessor = object : WorkflowProcessor<String, Int> {
            override fun process(input: Flow<String>): Flow<Int> {
                return input.map {
                    if (it == "invalid-data") {
                        throw IllegalArgumentException("Invalid data detected")
                    }
                    it.length
                }
            }
        }

        val writer = MockWriter()
        val engine = ApexFlowWorkflowEngine(reader, failingProcessor, writer)

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            engine.startAsync()
        }

        assertEquals("Invalid data detected", exception.message)
    }

    /**
     * Test workflow with empty input
     * Verifies that the workflow handles empty input gracefully
     */
    @Test
    fun testWorkflowWithEmptyInput() = runBlocking {
        // Given
        val reader = MockReader(emptyList())
        val processor = MockProcessor()
        val writer = MockWriter()

        val engine = ApexFlowWorkflowEngine(reader, processor, writer)

        // When
        engine.startAsync()

        // Then
        assertTrue(writer.results.isEmpty())
    }

    /**
     * Test workflow with single item input
     * Verifies that the workflow handles single item input correctly
     */
    @Test
    fun testWorkflowWithSingleItemInput() = runBlocking {
        // Given
        val reader = MockReader(listOf("single"))
        val processor = MockProcessor()
        val writer = MockWriter()

        val engine = ApexFlowWorkflowEngine(reader, processor, writer)

        // When
        engine.startAsync()

        // Then
        assertEquals(listOf(6), writer.results)
    }

    /**
     * Test getStatus method
     * Verifies that the status is properly returned
     */
    @Test
    fun testGetStatusMethod() = runBlocking {
        // Given
        val reader = MockReader(listOf("test"))
        val processor = MockProcessor()
        val writer = MockWriter()

        val engine = ApexFlowWorkflowEngine(reader, processor, writer)

        // When
        val status = engine.getStatus()

        // Then
        assertEquals(WorkflowStatus.IDLE, status)
    }

    /**
     * Test stop method
     * Verifies that the stop method works correctly (no-op in current implementation)
     */
    @Test
    fun testStopMethod() = runBlocking {
        // Given
        val reader = MockReader(listOf("test"))
        val processor = MockProcessor()
        val writer = MockWriter()

        val engine = ApexFlowWorkflowEngine(reader, processor, writer)

        // When
        engine.stop()
        // Then - no exception should be thrown
        engine.startAsync()

        // Verify workflow still works after stop
        assertEquals(listOf(4), writer.results)
    }
}
