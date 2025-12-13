package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.Flow

/**
 * Workflow data writer interface
 *
 * Responsible for writing processed data to target locations
 * Follows single responsibility principle: only responsible for data writing
 * Supports asynchronous stream writing, following Kotlin coroutine design pattern
 *
 * @param T Written data type
 */
interface WorkflowWriter<T> {
    /**
     * Write data flow
     *
     * @param data Flow<T> Data flow to write
     */
    suspend fun write(data: Flow<T>)
}
