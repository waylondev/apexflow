package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.runBlocking

/**
 * Workflow engine interface
 *
 * Responsible for coordinating the entire processing flow, connecting Reader, Processor and Writer
 * Follows single responsibility principle: only responsible for workflow coordination
 * Supports both asynchronous and blocking start modes
 *
 * Design follows dependency inversion principle:
 * - Depends on abstract interfaces (WorkflowReader, WorkflowProcessor, WorkflowWriter)
 * - Does not depend on concrete implementations
 * - Easy to extend and test
 */
interface WorkflowEngine {
    /**
     * Configure engine parameters
     *
     * @param config Engine configuration
     */
    fun configure(config: WorkflowConfig)

    /**
     * Start processing asynchronously
     */
    suspend fun startAsync()

    /**
     * Start processing synchronously, blocking until completed
     */
    fun start() = runBlocking {
        startAsync()
    }

    /**
     * Get processing status
     *
     * @return WorkflowStatus Engine status
     */
    fun getStatus(): WorkflowStatus

    /**
     * Stop the engine
     */
    fun stop()
}
