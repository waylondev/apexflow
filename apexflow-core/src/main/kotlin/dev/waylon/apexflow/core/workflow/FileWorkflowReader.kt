package dev.waylon.apexflow.core.workflow

/**
 * File workflow data reader interface
 *
 * Extends WorkflowReader, specialized for reading data from files
 * Follows interface segregation principle: only adds file-related functionality
 *
 * @param T Read data type
 */
interface FileWorkflowReader<T> : WorkflowReader<T> {
    /**
     * Set input file path
     *
     * @param filePath File path
     */
    fun setInput(filePath: String)
}
