package dev.waylon.apexflow.core.monitor

import dev.waylon.apexflow.core.workflow.WorkflowStatus

/**
 * Workflow stage enum
 */
enum class WorkflowStage {
    READING,
    PROCESSING,
    WRITING,
    COMPLETE
}

/**
 * Workflow performance metrics
 */
data class WorkflowMetrics(
    val stage: WorkflowStage,
    val startTime: Long,
    val endTime: Long,
    val itemsProcessed: Long = 0,
    val errors: Int = 0,
    val totalMemoryUsed: Long = 0
)

/**
 * Workflow monitor interface
 *
 * Provides methods for monitoring workflow execution performance
 * without using logs, to avoid performance overhead
 */
interface WorkflowMonitor {
    /**
     * Called when a workflow stage starts
     *
     * @param stage Current workflow stage
     * @param startTime Start time in milliseconds since epoch
     */
    fun onStageStart(stage: WorkflowStage, startTime: Long)
    
    /**
     * Called when a workflow stage completes
     *
     * @param metrics Workflow performance metrics
     */
    fun onStageComplete(metrics: WorkflowMetrics)
    
    /**
     * Called when an error occurs during workflow execution
     *
     * @param stage Current workflow stage
     * @param error Throwable that caused the error
     * @param timestamp Timestamp in milliseconds since epoch
     */
    fun onError(stage: WorkflowStage, error: Throwable, timestamp: Long)
    
    /**
     * Called when the entire workflow completes
     *
     * @param totalMetrics Aggregated metrics for the entire workflow
     */
    fun onWorkflowComplete(totalMetrics: WorkflowMetrics)
}