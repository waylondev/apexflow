package dev.waylon.apexflow.core.workflow

/**
 * Workflow engine status enum
 *
 * Defines possible states of the engine
 * Follows single responsibility principle: only responsible for status management
 */
enum class WorkflowStatus {
    /** Engine is in idle state, not started */
    IDLE,

    /** Engine is running */
    RUNNING,

    /** Engine has successfully completed processing */
    COMPLETED,

    /** Engine encountered an error during processing */
    FAILED,

    /** Engine was manually stopped */
    STOPPED
}
