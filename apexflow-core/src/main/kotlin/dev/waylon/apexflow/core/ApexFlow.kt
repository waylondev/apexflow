package dev.waylon.apexflow.core

import kotlinx.coroutines.flow.Flow

/**
 * Flow context marker for ApexFlow DSL
 * Used to restrict DSL scope
 */
@DslMarker
annotation class FlowDsl

/**
 * Top-level flow interface representing a complete workflow
 * Core design principle: "Everything is Flow" - pure transformation from Flow<T> to Flow<R>
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
     * Example: flow1 + flow2 + flow3
     *
     * @param next Next flow to execute after this one
     * @return Composed flow that executes both flows in sequence
     */
    operator fun <N> plus(next: ApexFlow<O, N>): ApexFlow<I, N> =
        compose(this, next)

    companion object {
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

        /**
         * Create an identity flow that passes through input unchanged
         * @return Identity flow
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


