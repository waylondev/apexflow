package dev.waylon.apexflow.core.workflow

/**
 * Batch workflow data processor interface
 *
 * Extends WorkflowProcessor, specialized for batch processing data
 * Follows interface segregation principle: only adds batch-related functionality
 *
 * @param I Input data type
 * @param O Output data type
 */
interface BatchWorkflowProcessor<I, O> : WorkflowProcessor<I, O> {
    /**
     * Set batch size
     *
     * @param batchSize Batch size
     */
    fun setBatchSize(batchSize: Int)
}
