package dev.waylon.apexflow.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for core ApexFlow functionality
 */
class ApexFlowCoreTest {

    /**
     * Test basic ApexFlow functionality
     */
    @Test
    fun `test basic transform`() = runBlocking {
        val flow = object : ApexFlow<Int, String> {
            override fun transform(input: Flow<Int>): Flow<String> {
                return input.map { "Processed: $it" }
            }
        }

        val result = flow.transform(flowOf(42)).toList()
        assertEquals(listOf("Processed: 42"), result)
    }

    /**
     * Test flow composition with + operator
     */
    @Test
    fun `test flow composition with plus operator`() = runBlocking {
        val flow1 = object : ApexFlow<Int, Int> {
            override fun transform(input: Flow<Int>): Flow<Int> {
                return input.map { it + 1 }
            }
        }

        val flow2 = object : ApexFlow<Int, String> {
            override fun transform(input: Flow<Int>): Flow<String> {
                return input.map { "Result: $it" }
            }
        }

        val composed = flow1 + flow2
        val result = composed.transform(flowOf(5)).toList()
        assertEquals(listOf("Result: 6"), result)
    }

    /**
     * Test multiple flow composition
     */
    @Test
    fun `test multiple flow composition`() = runBlocking {
        val flow1 = object : ApexFlow<Int, Int> {
            override fun transform(input: Flow<Int>): Flow<Int> {
                return input.map { it * 2 }
            }
        }

        val flow2 = object : ApexFlow<Int, Int> {
            override fun transform(input: Flow<Int>): Flow<Int> {
                return input.map { it + 3 }
            }
        }

        val flow3 = object : ApexFlow<Int, String> {
            override fun transform(input: Flow<Int>): Flow<String> {
                return input.map { "Final: $it" }
            }
        }

        val composed = flow1 + flow2 + flow3
        val result = composed.transform(flowOf(5)).toList()
        assertEquals(listOf("Final: 13"), result) // (5 * 2) + 3 = 13
    }

    /**
     * Test compose utility function
     */
    @Test
    fun `test compose utility function`() = runBlocking {
        val flow1 = object : ApexFlow<Int, Int> {
            override fun transform(input: Flow<Int>): Flow<Int> {
                return input.map { it + 5 }
            }
        }

        val flow2 = object : ApexFlow<Int, String> {
            override fun transform(input: Flow<Int>): Flow<String> {
                return input.map { "Composed: $it" }
            }
        }

        val composed = ApexFlow.compose(flow1, flow2)
        val result = composed.transform(flowOf(10)).toList()
        assertEquals(listOf("Composed: 15"), result)
    }

    /**
     * Test identity flow
     */
    @Test
    fun `test identity flow`() = runBlocking {
        val identity = ApexFlow.identity<Int>()
        val result = identity.transform(flowOf(42)).toList()
        assertEquals(listOf(42), result)
    }

    /**
     * Test identity flow composition
     */
    @Test
    fun `test identity flow composition`() = runBlocking {
        val flow = object : ApexFlow<Int, String> {
            override fun transform(input: Flow<Int>): Flow<String> {
                return input.map { "Processed: $it" }
            }
        }

        val identity = ApexFlow.identity<Int>()
        val composed1 = identity + flow
        val composed2 = flow + ApexFlow.identity<String>()

        val result1 = composed1.transform(flowOf(42)).toList()
        val result2 = composed2.transform(flowOf(42)).toList()

        assertEquals(listOf("Processed: 42"), result1)
        assertEquals(listOf("Processed: 42"), result2)
    }
}
