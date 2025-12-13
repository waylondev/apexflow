package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.Flow

/**
 * Workflow data processor interface
 *
 * Responsible for converting input data to output data
 * Follows single responsibility principle: only responsible for data conversion
 * Supports asynchronous stream processing, following Kotlin coroutine design pattern
 *
 * @param I Input data type
 * @param O Output data type
 */
interface WorkflowProcessor<I, O> {
    /**
     * Process input data flow, returning transformed output data flow
     *
     * @param input Flow<I> Input data flow
     * @return Flow<O> Output data flow
     */
    fun process(input: Flow<I>): Flow<O>

    /**
     * Companion object with utility functions
     */
    companion object {
        /**
         * Creates a no-op processor that passes input through unchanged
         *
         * @return WorkflowProcessor<I, I> Identity processor
         */
        @JvmStatic
        fun <I> identity(): WorkflowProcessor<I, I> = object : WorkflowProcessor<I, I> {
            override fun process(input: Flow<I>): Flow<I> = input
        }
    }
}

/**
 * Extension function to create a no-op processor
 *
 * @return WorkflowProcessor<I, I> Identity processor
 */
fun <I> WorkflowProcessor.Companion.noOp(): WorkflowProcessor<I, I> = identity()
