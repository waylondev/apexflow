package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.withPlugin
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApexFlowVisualizationPlugin
 */
class ApexFlowVisualizationPluginTest {

    /**
     * Test that Flow Visualization Plugin works without affecting normal flow execution
     */
    @Test
    fun `test flow visualization plugin works without affecting normal execution`() = runBlocking {
        // Create a normal flow
        val normalFlow = apexFlow<Int, Int> {
            map { "Processed: $it" }.map { it.length }
        }
        
        // Wrap with Flow Visualization Plugin using TEXT format
        val visualizedFlow = normalFlow.withPlugin(ApexFlowVisualizationPlugin())
        
        // Execute normally
        val results = visualizedFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(11, 11, 11), results)
    }
    
    /**
     * Test that Flow Visualization Plugin works with DOT output format
     */
    @Test
    fun `test flow visualization plugin works with dot format`() = runBlocking {
        // Create a normal flow
        val normalFlow = apexFlow<Int, Int> {
            map { it * 2 }
        }
        
        // Wrap with Flow Visualization Plugin using DOT format
        val visualizedFlow = normalFlow.withPlugin(ApexFlowVisualizationPlugin(
            outputFormat = ApexFlowVisualizationPlugin.VisualizationFormat.DOT
        ))
        
        // Execute normally
        val results = visualizedFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(2, 4, 6), results)
    }
    
    /**
     * Test that Flow Visualization Plugin works with composed flows
     */
    @Test
    fun `test flow visualization plugin works with composed flows`() = runBlocking {
        // Create multiple flows
        val flow1 = apexFlow<Int, Int> {
            map { it * 2 }
        }
        val flow2 = apexFlow<Int, Int> {
            map { it + 1 }
        }
        val flow3 = apexFlow<Int, Int> {
            map { it * 3 }
        }
        
        // Compose flows and wrap with Flow Visualization Plugin
        val composedFlow = (flow1 + flow2 + flow3).withPlugin(ApexFlowVisualizationPlugin())
        
        // Execute normally
        val results = composedFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        // Expected: (1*2+1)*3 = 9, (2*2+1)*3 = 15, (3*2+1)*3 = 21
        assertEquals(listOf(9, 15, 21), results)
    }
    
    /**
     * Test that Flow Visualization Plugin works with other plugins
     */
    @Test
    fun `test flow visualization plugin works with other plugins`() = runBlocking {
        // Create a normal flow
        val normalFlow = apexFlow<Int, Int> {
            map { it * 2 }
        }
        
        // Wrap with multiple plugins including Flow Visualization
        val multiPluginFlow = normalFlow
            .withPlugin(ApexLoggingPlugin())
            .withPlugin(ApexFlowVisualizationPlugin())
        
        // Execute normally
        val results = multiPluginFlow.transform(flowOf(1, 2, 3)).toList()
        
        // Verify results are correct
        assertEquals(3, results.size)
        assertEquals(listOf(2, 4, 6), results)
    }
}
