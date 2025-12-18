package dev.waylon.apexflow.core.dsl

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for core ApexFlow functionality
 * 
 * This test covers:
 * - Core DSL functionality
 * - Execution methods
 */
class ApexFlowCoreTest {

    /**
     * Test basic apexFlow DSL functionality
     */
    @Test
    fun `test basic apexFlow DSL`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            map { "Processed: $it" }
        }

        val result = workflow.transform(flowOf(42)).toList()
        assertEquals(listOf("Processed: 42"), result)
    }

    /**
     * Test execute with single value
     */
    @Test
    fun `test execute with single value`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            map { "Value: $it" }
        }

        val result = workflow.execute(42).toList()
        assertEquals(listOf("Value: 42"), result)
    }

    /**
     * Test execute with list of values
     */
    @Test
    fun `test execute with list`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            map { "List: $it" }
        }

        val result = workflow.execute(listOf(1, 2, 3)).toList()
        assertEquals(listOf("List: 1", "List: 2", "List: 3"), result)
    }

    /**
     * Test execute with varargs
     */
    @Test
    fun `test execute with varargs`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            map { "Vararg: $it" }
        }

        val result = workflow.execute(1, 2, 3, 4).toList()
        assertEquals(listOf("Vararg: 1", "Vararg: 2", "Vararg: 3", "Vararg: 4"), result)
    }
}
