package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApexFlowWorkflowEngine
 */
class ApexFlowWorkflowEngineTest {

    /**
     * Test basic workflow execution
     */
    @Test
    fun testBasicWorkflowExecution() = runBlocking {
        // Mock reader that emits 3 items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(1)
                emit(2)
                emit(3)
            }
        }

        // Mock processor that doubles each item
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { value -> value * 2 }
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

        // Create and configure workflow engine
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)

        // Start workflow
        engine.startAsync()

        // Verify results
        assertEquals(listOf(2, 4, 6), collectedItems)
    }

    /**
     * Test workflow with DSL configuration
     */
    @Test
    fun testWorkflowWithDslConfiguration() = runBlocking {
        // Mock components
        val mockReader = object : WorkflowReader<String> {
            override fun read(): Flow<String> = flow {
                emit("a")
                emit("b")
            }
        }

        val mockProcessor = object : WorkflowProcessor<String, String> {
            override fun process(input: Flow<String>): Flow<String> {
                return input.map { value -> value.uppercase() }
            }
        }

        val collectedItems = mutableListOf<String>()
        val mockWriter = object : WorkflowWriter<String> {
            override suspend fun write(data: Flow<String>) {
                data.collect {
                    collectedItems.add(it)
                }
            }
        }

        // Create and configure workflow engine
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)

        // Start workflow
        engine.startAsync()

        // Verify results
        assertEquals(listOf("A", "B"), collectedItems)
    }

    /**
     * Test workflow with error handling
     */
    @Test
    fun testWorkflowWithErrorHandling() = runBlocking {
        // Mock reader that emits items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(1)
                emit(2)
            }
        }

        // Mock processor that throws exception
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { value ->
                    if (value == 2) throw RuntimeException("Test exception")
                    value
                }
            }
        }

        // Mock writer
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

        // Expect exception
        val exception = runCatching {
            engine.startAsync()
        }.exceptionOrNull()

        // Verify exception is thrown
        assert(exception is RuntimeException)
    }
}
