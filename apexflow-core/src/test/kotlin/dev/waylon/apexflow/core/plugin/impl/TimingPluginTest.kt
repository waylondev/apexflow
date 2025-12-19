package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.withLogging
import dev.waylon.apexflow.core.dsl.withPlugin
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for ApexTimingPlugin functionality
 *
 * This test covers:
 * - Basic timing plugin usage
 * - withTiming convenience function
 * - Integration with other plugins
 */
class ApexTimingPluginTest {

    /**
     * Test basic ApexTimingPlugin functionality
     */
    @Test
    fun `test basic timing plugin`() = runBlocking {
        // Create a simple workflow
        val workflow = apexFlow<Int, String> {
            map { "$it" }
        }

        // Create timing plugin instance
        val timingPlugin = ApexTimingPlugin()
        val timedWorkflow = timingPlugin.wrap(workflow)

        // Execute the workflow
        val result = timedWorkflow.transform(flowOf(42)).toList()

        // Verify the result
        assertEquals(listOf("42"), result)
    }

    /**
     * Test withTiming convenience function
     */
    @Test
    fun `test withTiming convenience function`() = runBlocking {
        // Create a simple workflow with timing
        val workflow = apexFlow<Int, String> {
            map { "$it" }
        }

        // Use the timing plugin
        val timedWorkflow = workflow.withPlugin(ApexTimingPlugin())

        // Execute the workflow
        val result = timedWorkflow.transform(flowOf(42)).toList()

        // Verify the result
        assertEquals(listOf("42"), result)
    }

    /**
     * Test withTiming with custom logger name
     */
    @Test
    fun `test withTiming custom logger`() = runBlocking {
        // Create a simple workflow with timing and custom logger
        val workflow = apexFlow<Int, String> {
            map { "$it" }
        }

        // Use the timing plugin with custom logger
        val timedWorkflow = workflow.withPlugin(ApexTimingPlugin("custom.timing.logger"))

        // Execute the workflow
        val result = timedWorkflow.transform(flowOf(42)).toList()

        // Verify the result
        assertEquals(listOf("42"), result)
    }

    /**
     * Test timing plugin composition with other plugins
     */
    @Test
    fun `test timing plugin composition`() = runBlocking {
        // Create a simple workflow
        val workflow = apexFlow<Int, String> {
            map { "$it" }
        }

        // Apply multiple plugins
        val composedWorkflow = workflow
            .withPlugin(ApexTimingPlugin())
            .withLogging()
            .withPlugin(ApexTimingPlugin("second.timing.logger"))

        // Execute the workflow
        val result = composedWorkflow.transform(flowOf(42)).toList()

        // Verify the result
        assertEquals(listOf("42"), result)
    }
}
