package dev.waylon.apexflow.core.plugin.impl

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.withLogging
import dev.waylon.apexflow.core.dsl.withPlugin
import dev.waylon.apexflow.core.plugin.ApexFlowPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for ApexLoggingPlugin functionality
 *
 * This test covers:
 * - Basic logging plugin usage
 * - withLogging convenience function
 * - Plugin integration with apexFlow DSL
 */
class ApexLoggingPluginTest {

    /**
     * Test basic ApexLoggingPlugin functionality
     */
    @Test
    fun `test basic logging plugin`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            map { "Processed: $it" }
        }

        val loggingWorkflow = workflow.withPlugin(ApexLoggingPlugin())
        val result = loggingWorkflow.transform(flowOf(42)).toList()

        assertEquals(listOf("Processed: 42"), result)
        // Note: We're mainly testing that the plugin doesn't break functionality
        // Actual logging can be verified through integration tests
    }

    /**
     * Test withLogging convenience function
     */
    @Test
    fun `test withLogging convenience function`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            map { "Processed: $it" }
        }

        val loggingWorkflow = workflow.withLogging()
        val result = loggingWorkflow.transform(flowOf(42)).toList()

        assertEquals(listOf("Processed: 42"), result)
    }

    /**
     * Test logging plugin with custom logger name
     */
    @Test
    fun `test logging plugin with custom logger`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            map { "Processed: $it" }
        }

        val loggingWorkflow = workflow.withPlugin(ApexLoggingPlugin("custom.logger"))
        val result = loggingWorkflow.transform(flowOf(42)).toList()

        assertEquals(listOf("Processed: 42"), result)
    }

    /**
     * Test logging plugin with multiple values
     */
    @Test
    fun `test logging plugin with multiple values`() = runBlocking {
        val workflow = apexFlow<Int, Int> {
            map { it * 2 }
        }

        val loggingWorkflow = workflow.withLogging()
        val result = loggingWorkflow.transform(flowOf(1, 2, 3, 4, 5)).toList()

        assertEquals(listOf(2, 4, 6, 8, 10), result)
    }

    /**
     * Test logging plugin composition with other plugins
     */
    @Test
    fun `test logging plugin with other plugins`() = runBlocking {
        // Create a simple plugin for testing composition
        class PrefixPlugin(private val prefix: String) : ApexFlowPlugin {
            override fun <I, O> wrap(flow: ApexFlow<I, O>): ApexFlow<I, O> {
                return object : ApexFlow<I, O> {
                    override fun transform(input: Flow<I>): Flow<O> {
                        @Suppress("UNCHECKED_CAST")
                        return flow.transform(input).map { "$prefix$it" } as Flow<O>
                    }
                }
            }
        }

        val workflow = apexFlow<Int, String> {
            map { "$it" }
        }

        val composedWorkflow = workflow
            .withLogging()
            .withPlugin(PrefixPlugin("Prefixed: "))

        val result = composedWorkflow.transform(flowOf(42)).toList()
        assertEquals(listOf("Prefixed: 42"), result)
    }
}
