package dev.waylon.apexflow.core.dsl

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test suite for ApexFlow branch functionality
 * 
 * This test covers:
 * - Branch logic with whenFlow
 * - Value matching
 * - Predicate matching
 * - Traditional lambda syntax
 * - Edge cases
 */
class ApexFlowBranchTest {

    /**
     * Test whenFlow with value matching
     */
    @Test
    fun `test whenFlow with value matching`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            whenFlow {
                case(10) { flow -> flow.map { "Ten: $it" } }
                case(5) { flow -> flow.map { "Five: $it" } }
                case(0) { flow -> flow.map { "Zero: $it" } }
                elseCase { flow -> flow.map { "Other: $it" } }
            }
        }

        val testValues = listOf(10, 5, 0, 7)
        val expected = listOf("Ten: 10", "Five: 5", "Zero: 0", "Other: 7")
        
        val result = workflow.transform(testValues.asFlow()).toList()
        assertEquals(expected, result)
    }

    /**
     * Test whenFlow with predicate matching
     */
    @Test
    fun `test whenFlow with predicate matching`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            whenFlow {
                case({ it > 100 }) { flow -> flow.map { "Large: $it" } }
                case({ it > 50 }) { flow -> flow.map { "Medium: $it" } }
                case({ it > 10 }) { flow -> flow.map { "Small: $it" } }
                elseCase { flow -> flow.map { "Tiny: $it" } }
            }
        }

        val testValues = listOf(150, 75, 20, 5)
        val expected = listOf("Large: 150", "Medium: 75", "Small: 20", "Tiny: 5")
        
        val result = workflow.transform(testValues.asFlow()).toList()
        assertEquals(expected, result)
    }

    /**
     * Test whenFlow with mixed value and predicate matching
     */
    @Test
    fun `test whenFlow with mixed matching`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            whenFlow {
                case(42) { flow -> flow.map { "Answer: $it" } }
                case({ it % 2 == 0 }) { flow -> flow.map { "Even: $it" } }
                case({ it % 2 == 1 }) { flow -> flow.map { "Odd: $it" } }
                elseCase { flow -> flow.map { "Other: $it" } }
            }
        }

        val testValues = listOf(42, 10, 7)
        val expected = listOf("Answer: 42", "Even: 10", "Odd: 7")
        
        val result = workflow.transform(testValues.asFlow()).toList()
        assertEquals(expected, result)
    }

    /**
     * Test whenFlow with traditional lambda syntax
     */
    @Test
    fun `test whenFlow traditional syntax`() = runBlocking {
        val workflow = apexFlow<Int, String> {
            whenFlow {
                case({ it > 10 }) { flow ->
                    flow.map { "Large: $it" }
                }
                case({ it > 5 }) { flow ->
                    flow.map { "Medium: $it" }
                }
                elseCase { flow ->
                    flow.map { "Small: $it" }
                }
            }
        }

        val testValues = listOf(15, 7, 3)
        val expected = listOf("Large: 15", "Medium: 7", "Small: 3")
        
        val result = workflow.transform(testValues.asFlow()).toList()
        assertEquals(expected, result)
    }

    /**
     * Test edge cases
     */
    @Test
    fun `test edge cases`() = runBlocking {
        val workflow = apexFlow<Int?, String> {
            whenFlow {
                case(null) { flow -> flow.map { "Null: $it" } }
                case(0) { flow -> flow.map { "Zero: $it" } }
                case({ it != null && it > 0 }) { flow -> flow.map { "Positive: $it" } }
                elseCase { flow -> flow.map { "Negative: $it" } }
            }
        }

        val testValues = listOf(null, 0, 5, -5)
        val expected = listOf("Null: null", "Zero: 0", "Positive: 5", "Negative: -5")
        
        val result = workflow.transform(testValues.asFlow()).toList()
        assertEquals(expected, result)
    }

    /**
     * Test with complex data types
     */
    @Test
    fun `test complex data types`() = runBlocking {
        data class User(val id: Int, val name: String)
        data class UserDto(val id: Int, val name: String, val status: String)

        val workflow = apexFlow<User, UserDto> {
            whenFlow {
                case({ it.id > 100 }) { flow -> flow.map { UserDto(it.id, it.name, "Premium") } }
                case({ it.name.startsWith("A") }) { flow -> flow.map { UserDto(it.id, it.name, "VIP") } }
                elseCase { flow -> flow.map { UserDto(it.id, it.name, "Standard") } }
            }
        }

        val users = listOf(
            User(150, "John"),
            User(50, "Alice"),
            User(20, "Bob")
        )

        val expected = listOf(
            UserDto(150, "John", "Premium"),
            UserDto(50, "Alice", "VIP"),
            UserDto(20, "Bob", "Standard")
        )
        
        val result = workflow.transform(users.asFlow()).toList()
        assertEquals(expected, result)
    }
}
