package dev.waylon.apexflow.core.node

import kotlinx.coroutines.flow.Flow

/**
 * Flow node interface representing a single transformation node in a workflow
 * Used to execute specific transformation logic
 * Conforms to Clean Architecture's core layer, with no dependencies on external implementations
 * @param I Input type
 * @param O Output type
 */
interface FlowNode<I, O> {
    /**
     * Transform input Flow to output Flow, executing the transformation logic for a single node
     */
    fun transform(input: Flow<I>): Flow<O>
}
