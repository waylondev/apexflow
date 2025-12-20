package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.withPluginMemoryMonitoring
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApexMemoryMonitoringPlugin
 */
class ApexMemoryMonitoringPluginTest {

    /**
     * Test that Memory Monitoring Plugin works without affecting normal flow execution
     */
    @Test
    fun `test memory monitoring plugin works without affecting normal execution`() = runBlocking {
        // Create a normal flow
        val normalFlow = apexFlow<Int, Int> { 
            map { it -> it * 2 }
        }
        
        // Wrap with Memory Monitoring Plugin
        val memoryMonitoredFlow = normalFlow.withPluginMemoryMonitoring()
        
        // Execute normally
        val results = memoryMonitoredFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(2, 4, 6), results)
    }
    
    /**
     * Test that Memory Monitoring Plugin works with larger data sets
     */
    @Test
    fun `test memory monitoring plugin works with larger data sets`() = runBlocking {
        // Create a flow that processes larger data sets
        val dataFlow = apexFlow<Int, List<Int>> { 
            map { it -> 
                // Create a list with many elements to consume memory
                List(10000) { idx -> idx * it }
            }
        }
        
        // Wrap with Memory Monitoring Plugin
        val memoryMonitoredFlow = dataFlow.withPluginMemoryMonitoring()
        
        // Execute with multiple values
        val results = memoryMonitoredFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        results.forEachIndexed { index, list ->
            assertEquals(10000, list.size)
            assertEquals(0, list[0]) // First element should be 0 for all
            assertEquals(index + 1, list[1]) // Second element should be the input value
        }
    }
    
    /**
     * Test that Memory Monitoring Plugin works with different sampling intervals
     */
    @Test
    fun `test memory monitoring plugin works with different sampling intervals`() = runBlocking {
        // Create a flow with delay to allow sampling
        val delayFlow = apexFlow<Int, Int> { 
            map { it -> 
                // Add a small delay to allow sampling to occur
                Thread.sleep(100) 
                it * 2
            }
        }
        
        // Wrap with Memory Monitoring Plugin with custom sampling interval
        val memoryMonitoredFlow = delayFlow.withPluginMemoryMonitoring(
            samplingInterval = java.time.Duration.ofMillis(50)
        )
        
        // Execute with multiple values
        val results = memoryMonitoredFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(2, 4, 6), results)
    }
}
