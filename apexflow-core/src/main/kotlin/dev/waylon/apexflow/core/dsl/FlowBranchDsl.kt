package dev.waylon.apexflow.core.dsl

import dev.waylon.apexflow.core.ApexFlowDsl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * DSL for branch handling in ApexFlow, inspired by Kotlin's `when` expression
 *
 * These functions provide a type-safe, readable way to define branch logic in flow transformations,
 * allowing different paths to be taken based on input conditions.
 */

/**
 * DSL for branch handling in ApexFlow, inspired by Kotlin's `when` expression
 *
 * Provides type-safe, readable branch logic while maintaining the "Everything is Flow" principle.
 * This DSL allows defining conditional branches with different transformations, similar to a switch-case statement
 * but with Flow transformations as outcomes.
 *
 * Usage Example with traditional syntax:
 * ```kotlin
 * val workflow = apexFlow<Int, String> {
 *     whenFlow {
 *         case({ it > 10 }) {
 *             transformOnIO { "Large: $it (processed on IO)" }
 *         }
 *         case({ it > 5 }) {
 *             map { "Medium: $it" }
 *         }
 *         elseCase {
 *             map { "Small: $it" }
 *         }
 *     }
 * }
 * ```
 *
 * Usage Example with infix syntax (recommended for better readability):
 * ```kotlin
 * val workflow = apexFlow<Int, String> {
 *     whenFlow {
 *         case({ it > 10 }) then map { "Large: $it" }
 *         case({ it > 5 }) then map { "Medium: $it" }
 *         elseCase then map { "Small: $it" }
 *     }
 * }
 * ```
 *
 * @param configure Lambda to configure the branch cases
 * @return Flow with branch logic applied
 */
@ApexFlowDsl
@OptIn(ExperimentalCoroutinesApi::class)
fun <I, O> Flow<I>.whenFlow(
    configure: WhenFlowBuilder<I, O>.() -> Unit
): Flow<O> {
    val builder = WhenFlowBuilder<I, O>()
    builder.configure()
    
    // Use flatMapLatest for better performance with rapidly changing inputs
    // This cancels transformations for old values when new values arrive
    return this.flatMapLatest { input ->
        builder.evaluate(input)
    }
}

/**
 * Builder class for the whenFlow DSL
 *
 * This class follows the Builder Pattern, providing a fluent API for defining branch cases
 * and their corresponding transformations.
 *
 * @param I Input type of the Flow
 * @param O Output type of the Flow
 */
@ApexFlowDsl
class WhenFlowBuilder<I, O> {
    // Use private mutable list to store cases, ensuring encapsulation
    private val cases = mutableListOf<Pair<(I) -> Boolean, (Flow<I>) -> Flow<O>>>()
    private var elseBranch: ((Flow<I>) -> Flow<O>)? = null

    /**
     * Define a branch case with a condition and transformation
     * 
     * The first matching case will be executed. Cases are evaluated in the order they are defined.
     * 
     * @param condition Predicate function that determines if this case matches
     * @param transformation Flow transformation to apply if the condition is met
     */
    fun case(condition: (I) -> Boolean, transformation: (Flow<I>) -> Flow<O>) {
        cases.add(condition to transformation)
    }
    
    /**
     * Define a branch case with a specific value match and transformation
     * 
     * This allows for concise syntax when matching exact values: case(10, { map { "Exactly 10" } })
     * 
     * @param value Exact value to match
     * @param transformation Flow transformation to apply if the value matches
     */
    fun case(value: I, transformation: (Flow<I>) -> Flow<O>) {
        cases.add({ input: I -> input == value } to transformation)
    }
    
    /**
     * Define a branch case with a condition, returning a CaseHandler for infix operations
     * 
     * This allows for more readable syntax: case({ it > 10 }) then map { "Large: $it" }
     * 
     * @param condition Predicate function that determines if this case matches
     * @return CaseHandler for infix operations
     */
    fun case(condition: (I) -> Boolean): CaseHandler<I, O> {
        return CaseHandler(this, condition)
    }
    
    /**
     * Define a branch case with a specific value, returning a CaseHandler for infix operations
     * 
     * This allows for concise syntax: case(10) then map { "Exactly 10" }
     * 
     * @param value Exact value to match
     * @return CaseHandler for infix operations
     */
    fun case(value: I): CaseHandler<I, O> {
        return CaseHandler(this, { input: I -> input == value })
    }
    
    // Null handling is done through explicit predicate cases
    // Example: case({ it == null }) then map { "Null value" }
    // Example: case({ it != null }) then map { "Non-null: $it" }
    
    /**
     * Define the else branch - executed if no other case matches
     * 
     * This provides a fallback transformation for all inputs that don't match any case.
     * If no elseCase is defined and no cases match, an IllegalStateException will be thrown.
     * 
     * @param transformation Flow transformation to apply as the default case
     */
    fun elseCase(transformation: (Flow<I>) -> Flow<O>) {
        this.elseBranch = transformation
    }

    /**
     * Return an ElseHandler for infix operations
     *
     * This allows for more readable syntax: elseCase then map { "Small: $it" }
     *
     * @return ElseHandler for infix operations
     */
    val elseCase: ElseHandler<I, O> get() = ElseHandler(this)

    /**
     * Evaluate the input against all cases and return the matching transformation
     *
     * This method is internal and should not be called directly by users.
     *
     * @param input Input value to evaluate against the cases
     * @return Flow with the matching transformation applied
     * @throws IllegalStateException if no case matches and no elseCase is defined
     */
    internal fun evaluate(input: I): Flow<O> {
        // Find the first matching case
        val matchingCase = cases.firstOrNull { it.first(input) }

        // Get the transformation, or use elseBranch if no match found
        val transformation = matchingCase?.second ?: elseBranch
        ?: throw IllegalStateException("No matching case found for input: $input")

        // Create a single-element Flow for the input and apply the transformation
        return transformation(flowOf(input))
    }

    /**
     * Internal method to add a case
     */
    internal fun addCase(condition: (I) -> Boolean, transformation: (Flow<I>) -> Flow<O>) {
        cases.add(condition to transformation)
    }

    /**
     * Internal method to set the else branch
     */
    internal fun setElseBranch(transformation: (Flow<I>) -> Flow<O>) {
        this.elseBranch = transformation
    }
}

/**
 * Handler class for case infix operations
 *
 * Usage: case({ it > 10 }) then map { "Large: $it" }
 *
 * @param I Input type
 * @param O Output type
 */
@ApexFlowDsl
class CaseHandler<I, O>(
    private val builder: WhenFlowBuilder<I, O>,
    private val condition: (I) -> Boolean
) {
    /**
     * Infix function to define the transformation for a case
     * 
     * Usage: case({ it > 10 }) then map { "Large: $it" }
     * 
     * @param transformation Flow transformation to apply if the condition is met
     */
    infix fun then(transformation: (Flow<I>) -> Flow<O>) {
        builder.addCase(condition, transformation)
    }
}

/**
 * Handler class for else infix operations
 *
 * Usage: elseCase then map { "Small: $it" }
 *
 * @param I Input type
 * @param O Output type
 */
@ApexFlowDsl
class ElseHandler<I, O>(
    private val builder: WhenFlowBuilder<I, O>
) {
    /**
     * Infix function to define the transformation for the else case
     * 
     * Usage: elseCase then map { "Small: $it" }
     * 
     * @param transformation Flow transformation to apply as the default case
     */
    infix fun then(transformation: (Flow<I>) -> Flow<O>) {
        builder.setElseBranch(transformation)
    }
}