package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.dsl.apexFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApexResourceUtilizationPlugin
 */
class ApexResourceUtilizationPluginTest {

    /**
     * Test that Resource Utilization Plugin works without affecting normal flow execution
     */
    @Test
    fun `test resource utilization plugin works without affecting normal execution`() = runBlocking {
        // Create a normal flow
        val normalFlow = apexFlow<Int, Int> { 
            map { it -> it * 2 }
        }
        
        // Wrap with Resource Utilization Plugin
        val resourceMonitoredFlow = normalFlow.withPluginResourceUtilization()
        
        // Execute normally
        val results = resourceMonitoredFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(2, 4, 6), results)
    }
    
    /**
     * Test that Resource Utilization Plugin works with different sampling intervals
     */
    @Test
    fun `test resource utilization plugin works with different sampling intervals`() = runBlocking {
        // Create a flow with delay to allow sampling
        val delayFlow = apexFlow<Int, Int> { 
            map { it -> 
                // Add a small delay to allow sampling to occur
                Thread.sleep(150) 
                it * 2
            }
        }
        
        // Wrap with Resource Utilization Plugin with custom sampling interval
        val resourceMonitoredFlow = delayFlow.withPluginResourceUtilization(
            samplingInterval = java.time.Duration.ofSeconds(1)
        )
        
        // Execute with multiple values
        val results = resourceMonitoredFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(2, 4, 6), results)
    }
    
    /**
     * Test that Resource Utilization Plugin works with CPU-intensive operations
     */
    @Test
    fun `test resource utilization plugin works with cpu intensive operations`() = runBlocking {
        // Create a CPU-intensive flow
        val cpuIntensiveFlow = apexFlow<Int, Long> { 
            map { it -> 
                // CPU-intensive operation: calculate fibonacci
                fun fibonacci(n: Long): Long {
                    return if (n <= 1) n else fibonacci(n-1) + fibonacci(n-2)
                }
                fibonacci(it.toLong())
            }
        }
        
        // Wrap with Resource Utilization Plugin
        val resourceMonitoredFlow = cpuIntensiveFlow.withPluginResourceUtilization()
        
        // Execute with small values to avoid excessive execution time
        val results = resourceMonitoredFlow.transform(flowOf(10, 12, 15)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(55L, 144L, 610L), results)
    }
}
