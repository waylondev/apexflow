package dev.waylon.apexflow.core.node.impl

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for CacheFlowNode functionality
 * 
 * This test covers:
 * - Basic caching functionality
 * - Cache hit behavior
 * - Cache miss behavior
 * - Cache clearing
 * - Cache size tracking
 */
class CacheFlowNodeTest {

    /**
     * Test basic caching functionality
     */
    @Test
    fun `test basic caching`() = runBlocking {
        var transformationCount = 0
        
        val cacheNode = CacheFlowNode<Int, String> {
            transformationCount++
            "Processed: $it"
        }
        
        // First execution - should cache results
        val input1 = listOf(1, 2, 3, 1, 2, 3).asFlow()
        val result1 = cacheNode.transform(input1).toList()
        val expected1 = listOf("Processed: 1", "Processed: 2", "Processed: 3", "Processed: 1", "Processed: 2", "Processed: 3")
        
        assertEquals(expected1, result1)
        assertEquals(3, transformationCount) // Should only transform 3 unique values
        assertEquals(3, cacheNode.cacheSize()) // Cache should have 3 entries
    }
    
    /**
     * Test cache hit behavior
     */
    @Test
    fun `test cache hit`() = runBlocking {
        var transformationCount = 0
        
        val cacheNode = CacheFlowNode<Int, Int> {
            transformationCount++
            it * 2
        }
        
        // First execution - cache miss
        val input1 = listOf(5).asFlow()
        val result1 = cacheNode.transform(input1).toList()
        assertEquals(listOf(10), result1)
        assertEquals(1, transformationCount)
        assertEquals(1, cacheNode.cacheSize())
        
        // Second execution - cache hit
        val input2 = listOf(5).asFlow()
        val result2 = cacheNode.transform(input2).toList()
        assertEquals(listOf(10), result2)
        assertEquals(1, transformationCount) // Transformation count should not increase
        assertEquals(1, cacheNode.cacheSize()) // Cache size should remain the same
    }
    
    /**
     * Test cache clearing functionality
     */
    @Test
    fun `test cache clearing`() = runBlocking {
        var transformationCount = 0
        
        val cacheNode = CacheFlowNode<Int, String> {
            transformationCount++
            "Transformed: $it"
        }
        
        // Populate cache
        val input1 = listOf(1, 2, 3).asFlow()
        val result1 = cacheNode.transform(input1).toList()
        assertEquals(3, transformationCount)
        assertEquals(3, cacheNode.cacheSize())
        
        // Clear cache
        cacheNode.clearCache()
        assertEquals(0, cacheNode.cacheSize())
        
        // Should re-transform after clearing
        val input2 = listOf(1, 2, 3).asFlow()
        val result2 = cacheNode.transform(input2).toList()
        assertEquals(6, transformationCount) // Transformation count should increase again
        assertEquals(3, cacheNode.cacheSize()) // Cache should be repopulated
    }
    
    /**
     * Test cache size tracking
     */
    @Test
    fun `test cache size tracking`() = runBlocking {
        var transformationCount = 0
        
        val cacheNode = CacheFlowNode<Int, String> {
            transformationCount++
            "Value: $it"
        }
        
        // Start with empty cache
        assertEquals(0, cacheNode.cacheSize())
        
        // Add one entry
        cacheNode.transform(listOf(1).asFlow()).toList()
        assertEquals(1, cacheNode.cacheSize())
        
        // Add another unique entry
        cacheNode.transform(listOf(2).asFlow()).toList()
        assertEquals(2, cacheNode.cacheSize())
        
        // Add duplicate entry - cache size should not change
        cacheNode.transform(listOf(1).asFlow()).toList()
        assertEquals(2, cacheNode.cacheSize())
        
        // Clear cache
        cacheNode.clearCache()
        assertEquals(0, cacheNode.cacheSize())
    }
    
    /**
     * Test caching with null values
     */
    @Test
    fun `test caching with null values`() = runBlocking {
        var transformationCount = 0
        
        val cacheNode = CacheFlowNode<Int, String?> {
            transformationCount++
            if (it == 0) null else "Value: $it"
        }
        
        // Test with null result
        val input1 = listOf(0, 0).asFlow()
        val result1 = cacheNode.transform(input1).toList()
        assertEquals(listOf(null, null), result1)
        assertEquals(1, transformationCount) // Should only transform once
        assertEquals(1, cacheNode.cacheSize()) // Should cache null value
        
        // Test with non-null result
        val input2 = listOf(1, 1).asFlow()
        val result2 = cacheNode.transform(input2).toList()
        assertEquals(listOf("Value: 1", "Value: 1"), result2)
        assertEquals(2, transformationCount) // Should transform once more
        assertEquals(2, cacheNode.cacheSize()) // Should have two entries
    }
}
