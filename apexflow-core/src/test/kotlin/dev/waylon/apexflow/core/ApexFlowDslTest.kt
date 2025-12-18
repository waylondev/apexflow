package dev.waylon.apexflow.core

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.whenFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Test suite for ApexFlow DSL functionality
 */
class ApexFlowDslTest {

    /**
     * Test basic DSL builder functionality
     */
    @Test
    fun `test basic DSL builder`() = runBlocking {
        val flow = apexFlow<Int, String> {
            map { "Processed: $it" }
        }

        val result = flow.transform(flowOf(42)).toList()
        assertEquals(listOf("Processed: 42"), result)
    }

    /**
     * Test DSL composition
     */
    @Test
    fun `test DSL composition`() = runBlocking {
        val flow1 = apexFlow<Int, Int> {
            map { it + 1 }
        }

        val flow2 = apexFlow<Int, String> {
            map { "Result: $it" }
        }

        val composed = flow1 + flow2
        val result = composed.transform(flowOf(5)).toList()
        assertEquals(listOf("Result: 6"), result)
    }

    /**
     * Test whenFlow DSL with traditional syntax
     */
    @Test
    fun `test whenFlow DSL with traditional syntax`() = runBlocking {
        val flow = flowOf(3, 7, 15)
            .whenFlow {
                case({ input -> input > 10 }) then { it.map { "Large: $it" } }
                case({ input -> input > 5 }) then { it.map { "Medium: $it" } }
                elseCase then { it.map { "Small: $it" } }
            }

        val result = flow.toList()
        assertEquals(listOf("Small: 3", "Medium: 7", "Large: 15"), result)
    }

    /**
     * Test whenFlow DSL with infix syntax
     */
    @Test
    fun `test whenFlow DSL with infix syntax`() = runBlocking {
        val flow = flowOf(3, 7, 15)
            .whenFlow {
                case({ input -> input > 10 }) then { it.map { "Large: $it" } }
                case({ input -> input > 5 }) then { it.map { "Medium: $it" } }
                elseCase then { it.map { "Small: $it" } }
            }

        val result = flow.toList()
        assertEquals(listOf("Small: 3", "Medium: 7", "Large: 15"), result)
    }

    /**
     * Test whenFlow DSL with only else case
     */
    @Test
    fun `test whenFlow DSL with only else case`() = runBlocking {
        val flow = flowOf(1, 2, 3)
            .whenFlow {
                elseCase then { it.map { "Default: $it" } }
            }

        val result = flow.toList()
        assertEquals(listOf("Default: 1", "Default: 2", "Default: 3"), result)
    }

    /**
     * Test whenFlow DSL with no matching cases - should throw exception
     */
    @Test
    fun `test whenFlow DSL with no matching cases should throw exception`() {
        val flow = flowOf(1, 2, 3)
            .whenFlow {
                case({ input -> input > 5 }) then { it.map { "Greater than 5: $it" } }
                case({ input -> input > 10 }) then { it.map { "Greater than 10: $it" } }
            }

        assertThrows(IllegalStateException::class.java) {
            runBlocking { flow.toList() }
        }
    }

    /**
     * Test whenFlow DSL with multiple matches - should only return first match
     */
    @Test
    fun `test whenFlow DSL with multiple matches`() = runBlocking {
        val flow = flowOf(15)
            .whenFlow {
                case({ input -> input > 5 }) then { it.map { "Greater than 5: $it" } }
                case({ input -> input > 10 }) then { it.map { "Greater than 10: $it" } }
                case({ input -> input > 15 }) then { it.map { "Greater than 15: $it" } }
            }

        val result = flow.toList()
        assertEquals(listOf("Greater than 5: 15"), result) // Only first match is returned
    }

    /**
     * Test execute function with DSL
     */
    @Test
    fun `test execute function with DSL`() = runBlocking {
        val flow = apexFlow<Int, String> {
            map { "Processed: $it" }
        }

        val result = flow.execute(flowOf(42)).toList().first()
        assertEquals("Processed: 42", result)
    }

    /**
     * Test execute function with flow input
     */
    @Test
    fun `test execute function with flow input`() = runBlocking {
        val flow = apexFlow<Int, String> {
            map { "Processed: $it" }
        }

        val result = flow.execute(flowOf(42)).toList()
        assertEquals(listOf("Processed: 42"), result)
    }
}
