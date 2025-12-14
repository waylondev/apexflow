package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Edge case tests for workflow engine
 */
class WorkflowEdgeCaseTest {

    /**
     * Test workflow with empty input
     *
     * This test verifies that the workflow engine can handle empty input gracefully
     */
    @Test
    fun testWorkflowWithEmptyInput() = runBlocking {
        // Mock reader that emits nothing
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = emptyFlow()
        }

        // Mock processor
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { it + 1 }
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

        // Verify results - no items should be collected
        assertEquals(0, collectedItems.size)
    }

    /**
     * Test workflow with single item
     *
     * This test verifies that the workflow engine can handle a single item
     */
    @Test
    fun testWorkflowWithSingleItem() = runBlocking {
        // Mock reader that emits a single item
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(42)
            }
        }

        // Mock processor that processes the item
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { it * 2 }
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
        assertEquals(listOf(84), collectedItems)
    }

    /**
     * Test workflow with reader that throws exception
     *
     * This test verifies that the workflow engine handles exceptions from the reader
     */
    @Test
    fun testWorkflowWithReaderException() = runBlocking {
        // Mock reader that throws exception
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(1)
                throw RuntimeException("Reader exception")
            }
        }

        // Mock processor
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { it + 1 }
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
        assertThrows<RuntimeException> {
            runBlocking {
                engine.startAsync()
            }
        }
    }

    /**
     * Test workflow with processor that throws exception
     *
     * This test verifies that the workflow engine handles exceptions from the processor
     */
    @Test
    fun testWorkflowWithProcessorException() = runBlocking {
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
                return input.map {
                    if (it == 2) throw RuntimeException("Processor exception")
                    it + 1
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
        assertThrows<RuntimeException> {
            runBlocking {
                engine.startAsync()
            }
        }
    }

    /**
     * Test workflow with writer that throws exception
     *
     * This test verifies that the workflow engine handles exceptions from the writer
     */
    @Test
    fun testWorkflowWithWriterException() = runBlocking {
        // Mock reader that emits items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(1)
                emit(2)
            }
        }

        // Mock processor
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { it + 1 }
            }
        }

        // Mock writer that throws exception
        val mockWriter = object : WorkflowWriter<Int> {
            override suspend fun write(data: Flow<Int>) {
                data.collect {
                    if (it == 2) throw RuntimeException("Writer exception")
                }
            }
        }

        // Create workflow engine
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)

        // Expect exception
        assertThrows<RuntimeException> {
            runBlocking {
                engine.startAsync()
            }
        }

        // Verify that exception is thrown - no need to check collected items
        // due to parallel processing nature, collected items may vary
    }

    /**
     * Test workflow with large items
     *
     * This test verifies that the workflow engine can handle large items
     */
    @Test
    fun testWorkflowWithLargeItems() = runBlocking {
        // Size of each large item
        val largeItemSize = 1024 * 1024 // 1MB

        // Mock reader that emits large strings
        val mockReader = object : WorkflowReader<String> {
            override fun read(): Flow<String> = flow {
                // Create a large string
                val largeString = "a".repeat(largeItemSize)

                // Emit the large string multiple times
                repeat(3) {
                    emit(largeString)
                }
            }
        }

        // Mock processor that processes large strings
        val mockProcessor = object : WorkflowProcessor<String, String> {
            override fun process(input: Flow<String>): Flow<String> {
                return input.map { it.substring(0, 10) } // Take first 10 characters
            }
        }

        // Mock writer that collects items
        val collectedItems = mutableListOf<String>()
        val mockWriter = object : WorkflowWriter<String> {
            override suspend fun write(data: Flow<String>) {
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
        assertEquals(3, collectedItems.size)
        collectedItems.forEach { assertEquals("aaaaaaaaaa", it) }
    }
}
