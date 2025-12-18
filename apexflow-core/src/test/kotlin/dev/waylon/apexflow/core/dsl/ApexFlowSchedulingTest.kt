package dev.waylon.apexflow.core.dsl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for ApexFlow scheduling functionality
 * 
 * This test covers:
 * - Scheduler management
 * - TransformOn with explicit dispatcher
 * - transformOnIO convenience function
 * - transformOnDefault convenience function
 */
class ApexFlowSchedulingTest {

    /**
     * Test transformOn with explicit dispatcher
     */
    @Test
    fun `test transformOn with explicit dispatcher`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            transformOn(Dispatchers.IO) { input ->
                "IO: $input"
            }
        }

        val result = workflow.transform(flowOf(42)).toList()
        assertEquals(listOf("IO: 42"), result)
    }

    /**
     * Test transformOnIO convenience function
     */
    @Test
    fun `test transformOnIO`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            transformOnIO { input ->
                "IO: $input"
            }
        }

        val result = workflow.transform(flowOf(42)).toList()
        assertEquals(listOf("IO: 42"), result)
    }

    /**
     * Test transformOnDefault convenience function
     */
    @Test
    fun `test transformOnDefault`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            transformOnDefault { input ->
                "Default: $input"
            }
        }

        val result = workflow.transform(flowOf(42)).toList()
        assertEquals(listOf("Default: 42"), result)
    }
}
