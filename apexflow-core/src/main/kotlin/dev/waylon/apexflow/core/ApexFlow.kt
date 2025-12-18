package dev.waylon.apexflow.core

import kotlinx.coroutines.flow.Flow

/**
 * Flow context marker for ApexFlow DSL
 * Used with context receivers to improve DSL experience in Kotlin 2.3.0
 */
@DslMarker
annotation class FlowDsl

/**
 * Flow execution context for configuring workflows
 * Leverages Kotlin 2.3.0 context receivers for better DSL experience
 */
interface FlowContext {
    /** Maximum concurrency level for parallel operations */
    val maxConcurrency: Int get() = 4

    /** Whether to enable debug logging */
    val debugMode: Boolean get() = false
}

/**
 * Default implementation of FlowContext
 */
val DefaultFlowContext: FlowContext = object : FlowContext {}

/**
 * Top-level flow interface representing a complete workflow, used to orchestrate multiple FlowNodes
 * Conforms to Clean Architecture's core layer, with no dependencies on external implementations
 * Using Kotlin 2.3.0 modern features including context receivers and improved type system
 *
 * @param I Input type
 * @param O Output type
 */
interface ApexFlow<I, O> {
    /**
     * Transform input Flow to output Flow, executing the complete workflow
     * @param input Input Flow containing data to process
     * @return Output Flow with processed data
     */
    fun transform(input: Flow<I>): Flow<O>

    /**
     * Compose this flow with another flow to create a new composed flow
     * @param next Next flow to execute after this one
     * @return Composed flow that executes both flows in sequence
     */
    operator fun <N> plus(next: ApexFlow<O, N>): ApexFlow<I, N> =
        ApexFlow.compose(this, next)

    /**
     * Modern nested type aliases for simplifying complex Flow type declarations
     * Leveraging Kotlin 2.3.0's nested type alias feature
     */
    companion object {
        /** Identity flow that passes through input unchanged */
        typealias Identity<I> = ApexFlow<I, I>

        /** Basic flow chain with single input/output */
        typealias Chain<I, O> = ApexFlow<I, O>

        /** Composed flow that combines two flows */
        typealias Composed<I, M, O> = ApexFlow<I, O>

        /** Function type for composing two flows */
        typealias Composer<I, M, O> = (ApexFlow<I, M>, ApexFlow<M, O>) -> ApexFlow<I, O>

        /**
         * Compose two flows into a single composed flow
         * @param first First flow to execute
         * @param second Second flow to execute after the first
         * @return Composed flow
         */
        fun <I, M, O> compose(first: ApexFlow<I, M>, second: ApexFlow<M, O>): ApexFlow<I, O> {
            return object : ApexFlow<I, O> {
                override fun transform(input: Flow<I>): Flow<O> {
                    return second.transform(first.transform(input))
                }
            }
        }
    }
}
