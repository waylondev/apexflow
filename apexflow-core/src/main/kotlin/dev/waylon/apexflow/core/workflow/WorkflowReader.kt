package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.Flow

/**
 * Workflow data reader interface
 *
 * Responsible for reading data from various sources and converting to a unified stream format
 * Follows single responsibility principle: only responsible for data reading
 * Supports asynchronous stream reading, following Kotlin coroutine design pattern
 *
 * @param T Read data type
 */
interface WorkflowReader<T> {
    /**
     * Read data, returning a Flow that supports asynchronous and backpressure
     *
     * @return Flow<T> Data flow
     */
    fun read(): Flow<T>
}
