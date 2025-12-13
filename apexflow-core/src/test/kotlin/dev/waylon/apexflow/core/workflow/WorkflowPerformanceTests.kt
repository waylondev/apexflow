package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Comprehensive performance tests for workflow engine
 *
 * These tests verify the performance characteristics of the workflow engine
 * under various conditions, including large datasets, backpressure, and edge cases.
 */
class WorkflowPerformanceTests {
    
    /**
     * Test performance with large dataset (10,000 items)
     *
     * This test measures the time it takes to process 10,000 items
     * to verify that the workflow engine can handle large datasets efficiently.
     */
    @Test
    fun testPerformanceWithLargeDataset() = runBlocking {
        val itemCount = 10000
        
        // Mock reader that emits many items
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
        
        // Mock writer that collects items
        val collectedItems = mutableListOf<Int>()
        val mockWriter = object : WorkflowWriter<Int> {
            override suspend fun write(data: Flow<Int>) {
                data.collect {
                    collectedItems.add(it)
                }
            }
        }
        
        // Create workflow engine with optimized buffers
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)
        engine.configure(WorkflowConfig(
            readBufferSize = 1000,
            processBufferSize = 1000
        ))
        
        // Measure execution time
        val startTime = System.currentTimeMillis()
        
        // Start workflow
        engine.startAsync()
        
        // Calculate execution time
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Print performance metrics
        println("Processed $itemCount items in $executionTime ms")
        println("Throughput: ${itemCount / maxOf(executionTime, 1)} items/ms")
        
        // Verify results
        assertEquals(itemCount, collectedItems.size)
        assertEquals(1, collectedItems.first())
        assertEquals(itemCount, collectedItems.last())
    }
    
    /**
     * Test backpressure with slow writer
     *
     * This test verifies that the workflow engine can handle backpressure
     * when the writer is slower than the reader and processor.
     */
    @Test
    fun testBackpressureWithSlowWriter() = runBlocking {
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
                    delay(1) // Simulate 1ms per item write
                    collectedItems.add(it)
                }
            }
        }
        
        // Create workflow engine with small buffers to trigger backpressure
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)
        engine.configure(WorkflowConfig(
            readBufferSize = 10,
            processBufferSize = 10
        ))
        
        // Measure execution time
        val startTime = System.currentTimeMillis()
        
        // Start workflow
        engine.startAsync()
        
        // Calculate execution time
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Print performance metrics
        println("Slow writer - Processed $itemCount items in $executionTime ms")
        
        // Verify results
        assertEquals(itemCount, collectedItems.size)
    }
    
    /**
     * Test backpressure with slow processor
     *
     * This test verifies that the workflow engine can handle backpressure
     * when the processor is slower than the reader.
     */
    @Test
    fun testBackpressureWithSlowProcessor() = runBlocking {
        val itemCount = 1000
        
        // Mock reader that emits items quickly
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                repeat(itemCount) {
                    emit(it)
                }
            }
        }
        
        // Mock processor that is slow
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.onEach { delay(1) } // Simulate 1ms per item processing
                    .map { it + 1 }
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
        
        // Create workflow engine
        val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)
        
        // Measure execution time
        val startTime = System.currentTimeMillis()
        
        // Start workflow
        engine.startAsync()
        
        // Calculate execution time
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Print performance metrics
        println("Slow processor - Processed $itemCount items in $executionTime ms")
        
        // Verify results
        assertEquals(itemCount, collectedItems.size)
    }
    
    /**
     * Test edge case with empty input
     *
     * This test verifies that the workflow engine handles empty input gracefully.
     */
    @Test
    fun testEmptyInput() = runBlocking {
        // Mock reader that emits nothing
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {}
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
     * Test edge case with single item
     *
     * This test verifies that the workflow engine handles a single item correctly.
     */
    @Test
    fun testSingleItem() = runBlocking {
        // Mock reader that emits a single item
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(42)
            }
        }
        
        // Mock processor
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
     * Test with very large items (1MB each)
     *
     * This test verifies that the workflow engine handles large items efficiently.
     */
    @Test
    fun testLargeItems() = runBlocking {
        val itemCount = 10
        val itemSize = 1024 * 1024 // 1MB per item
        
        // Mock reader that emits large strings
        val mockReader = object : WorkflowReader<String> {
            override fun read(): Flow<String> = flow {
                repeat(itemCount) {
                    emit("a".repeat(itemSize)) // 1MB string
                }
            }
        }
        
        // Mock processor that takes first 10 characters
        val mockProcessor = object : WorkflowProcessor<String, String> {
            override fun process(input: Flow<String>): Flow<String> {
                return input.map { it.substring(0, 10) }
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
        
        // Measure execution time
        val startTime = System.currentTimeMillis()
        
        // Start workflow
        engine.startAsync()
        
        // Calculate execution time
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Print performance metrics
        println("Processed $itemCount items of 1MB each in $executionTime ms")
        
        // Verify results
        assertEquals(itemCount, collectedItems.size)
        collectedItems.forEach { assertEquals("aaaaaaaaaa", it) }
    }
    
    /**
     * Test exception handling from reader
     *
     * This test verifies that exceptions from the reader are properly handled.
     */
    @Test
    fun testReaderException() = runBlocking {
        // Mock reader that throws exception
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(1)
                emit(2)
                throw RuntimeException("Reader exception")
            }
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
        
        // Expect exception
        assertThrows<RuntimeException> {
            runBlocking {
                engine.startAsync()
            }
        }
        
        // Note: Due to parallel processing nature, collectedItems may be empty or contain some items
        // We don't assert on collectedItems size, only that exception is properly thrown
    }
    
    /**
     * Test exception handling from processor
     *
     * This test verifies that exceptions from the processor are properly handled.
     */
    @Test
    fun testProcessorException() = runBlocking {
        // Mock reader that emits items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(1)
                emit(2)
                emit(3)
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
        
        // Expect exception
        assertThrows<RuntimeException> {
            runBlocking {
                engine.startAsync()
            }
        }
        
        // Note: Due to parallel processing nature, collectedItems may be empty or contain some items
        // We don't assert on collectedItems size, only that exception is properly thrown
    }
    
    /**
     * Test exception handling from writer
     *
     * This test verifies that exceptions from the writer are properly handled.
     */
    @Test
    fun testWriterException() = runBlocking {
        // Mock reader that emits items
        val mockReader = object : WorkflowReader<Int> {
            override fun read(): Flow<Int> = flow {
                emit(1)
                emit(2)
                emit(3)
            }
        }
        
        // Mock processor
        val mockProcessor = object : WorkflowProcessor<Int, Int> {
            override fun process(input: Flow<Int>): Flow<Int> {
                return input.map { it + 1 }
            }
        }
        
        // Mock writer that throws exception
        val collectedItems = mutableListOf<Int>()
        val mockWriter = object : WorkflowWriter<Int> {
            override suspend fun write(data: Flow<Int>) {
                data.collect {
                    if (it == 2) throw RuntimeException("Writer exception")
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
        
        // Note: Due to parallel processing nature, collectedItems may be empty or contain some items
        // We don't assert on collectedItems size, only that exception is properly thrown
    }
    
    /**
     * Test performance with varying buffer sizes
     *
     * This test compares performance with different buffer sizes to identify the optimal configuration.
     */
    @Test
    fun testPerformanceWithDifferentBufferSizes() = runBlocking {
        val itemCount = 5000
        
        // Test different buffer sizes
        val bufferSizes = listOf(10, 100, 500, 1000, 2000)
        
        bufferSizes.forEach { bufferSize ->
            // Mock components
            val mockReader = object : WorkflowReader<Int> {
                override fun read(): Flow<Int> = flow {
                    repeat(itemCount) {
                        emit(it)
                    }
                }
            }
            
            val mockProcessor = object : WorkflowProcessor<Int, Int> {
                override fun process(input: Flow<Int>): Flow<Int> {
                    return input.map { it + 1 }
                }
            }
            
            val mockWriter = object : WorkflowWriter<Int> {
                override suspend fun write(data: Flow<Int>) {
                    data.collect { /* No-op */ }
                }
            }
            
            // Create workflow engine with current buffer size
            val engine = ApexFlowWorkflowEngine(mockReader, mockProcessor, mockWriter)
            engine.configure(WorkflowConfig(
                readBufferSize = bufferSize,
                processBufferSize = bufferSize
            ))
            
            // Measure execution time
            val startTime = System.currentTimeMillis()
            
            // Start workflow
            engine.startAsync()
            
            // Calculate execution time
            val endTime = System.currentTimeMillis()
            val executionTime = endTime - startTime
            
            // Print performance metrics
            println("Buffer size $bufferSize - Processed $itemCount items in $executionTime ms")
        }
    }
}
