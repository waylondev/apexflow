package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.dsl.apexFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApexErrorContextPlugin
 */
class ApexErrorContextPluginTest {

    /**
     * Test that Error Context Plugin provides detailed error information
     */
    @Test
    fun `test error context plugin provides detailed error information`() = runBlocking {
        // Create a flow that will throw an exception
        val errorFlow = apexFlow<Int, Int> { 
            map { it -> 
                if (it == 2) {
                    throw RuntimeException("Test exception for value 2")
                }
                it * 2
            }
        }
        
        // Wrap with Error Context Plugin
        val errorHandledFlow = errorFlow.withPluginErrorContext()
        
        // Execute and catch the expected exception
        try {
            errorHandledFlow.transform(flowOf(1, 2, 3)).toList()
            // Should not reach here - exception expected
            assert(false) { "Expected exception was not thrown" }
        } catch (e: RuntimeException) {
            // Exception should be re-thrown after being logged with context
            assertEquals("Test exception for value 2", e.message)
        }
    }
    
    /**
     * Test that Error Context Plugin works with normal flow execution
     */
    @Test
    fun `test error context plugin works with normal execution`() = runBlocking {
        // Create a normal flow
        val normalFlow = apexFlow<Int, Int> { 
            map { it -> it * 2 }
        }
        
        // Wrap with Error Context Plugin
        val errorHandledFlow = normalFlow.withPluginErrorContext()
        
        // Execute normally
        val results = errorHandledFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results
        assertEquals(3, results.size)
        assertEquals(listOf(2, 4, 6), results)
    }
}
