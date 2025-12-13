package dev.waylon.apexflow.core.workflow

/**
 * File workflow data writer interface
 *
 * Extends WorkflowWriter, specialized for writing data to files
 * Follows interface segregation principle: only adds file-related functionality
 *
 * @param T Written data type
 */
interface FileWorkflowWriter<T> : WorkflowWriter<T> {
    /**
     * Set output file path
     *
     * @param filePath File path
     */
    fun setOutput(filePath: String)
}
