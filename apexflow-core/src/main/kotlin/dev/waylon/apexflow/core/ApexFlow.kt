package dev.waylon.apexflow.core

import kotlinx.coroutines.flow.Flow

/**
 * Flow context marker for ApexFlow DSL
 * Used to restrict DSL scope and reduce this keyword ambiguity
 *
 * @see [DslMarker] for more information about DSL markers
 */
@DslMarker
annotation class ApexFlowDsl

/**
 * Top-level flow interface representing a complete workflow
 * Core design principle: "Everything is Flow" - pure transformation from Flow<T> to Flow<R>
 *
 * This interface follows the Single Responsibility Principle, focusing solely on flow transformation.
 * It supports composition through the + operator, allowing complex workflows to be built from simple components.
 *
 * @param I Input type - the type of elements in the input Flow
 * @param O Output type - the type of elements in the output Flow
 *
 * @sample [dev.waylon.apexflow.core.dsl.apexFlow] for usage examples
 */
interface ApexFlow<I, O> {
    /**
     * Transform input Flow to output Flow, executing the complete workflow
     *
     * This is the core method of ApexFlow, implementing the "Flow<I> to Flow<O>" transformation.
     * Subclasses should implement this method to define their specific transformation logic.
     *
     * @param input Input Flow containing data to process
     * @return Output Flow with processed data
     *
     * @see [kotlinx.coroutines.flow.Flow] for Flow API documentation
     */
    fun transform(input: Flow<I>): Flow<O>

    /**
     * Compose this flow with another flow to create a new composed flow
     *
     * Usage: flow1 + flow2 + flow3
     *
     * @param next Next flow to execute after this one
     * @return Composed flow that executes both flows in sequence
     */
    operator fun <N> plus(next: ApexFlow<O, N>): ApexFlow<I, N> =
        compose(this, next)

    /**
     * Companion object providing utility methods for ApexFlow
     *
     * This follows the Factory Pattern, providing convenient methods to create ApexFlow instances.
     */
    companion object {
        /**
         * Compose two flows into a single composed flow
         *
         * @param first First flow to execute
         * @param second Second flow to execute after the first
         * @return Composed flow that executes both flows in sequence
         */
        fun <I, M, O> compose(first: ApexFlow<I, M>, second: ApexFlow<M, O>): ApexFlow<I, O> {
            return object : ApexFlow<I, O> {
                override fun transform(input: Flow<I>): Flow<O> {
                    return second.transform(first.transform(input))
                }
            }
        }

        /**
         * Create an identity flow that passes through input unchanged
         *
         * This is useful as a starting point for flow composition or as a no-op placeholder.
         *
         * @return Identity flow that returns input unchanged
         */
        fun <I> identity(): ApexFlow<I, I> {
            return object : ApexFlow<I, I> {
                override fun transform(input: Flow<I>): Flow<I> {
                    return input
                }
            }
        }
    }
}


