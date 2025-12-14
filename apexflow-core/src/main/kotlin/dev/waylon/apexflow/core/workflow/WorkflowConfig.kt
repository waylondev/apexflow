package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Workflow engine configuration class
 *
 * Contains all configuration parameters required for engine operation
 * Follows single responsibility principle: only responsible for configuration management
 * Supports default values to simplify configuration process
 */
data class WorkflowConfig(
    /**
     * Read buffer size
     * Used to balance reader and processor speeds, default is 10
     */
    val readBufferSize: Int = 10,

    /**
     * Process buffer size
     * Used to balance processor and writer speeds, default is 10
     */
    val processBufferSize: Int = 10,

    /**
     * Error handling function
     * Used to handle exceptions during processing, default prints stack trace
     */
    val errorHandler: (Throwable) -> Unit = { it.printStackTrace() },

    /**
     * Coroutine dispatcher for reading operations
     * Default: Dispatchers.IO for IO-intensive operations
     */
    val readDispatcher: CoroutineDispatcher = Dispatchers.IO,

    /**
     * Coroutine dispatcher for processing operations
     * Default: Dispatchers.Default for CPU-intensive operations
     */
    val processDispatcher: CoroutineDispatcher = Dispatchers.Default,

    /**
     * Coroutine dispatcher for writing operations
     * Default: Dispatchers.IO for IO-intensive operations
     */
    val writeDispatcher: CoroutineDispatcher = Dispatchers.IO,

    /**
     * IO buffer size (bytes) for reading files
     * Default: 4 * 8192 = 32768 bytes
     */
    val ioBufferSize: Int = 4 * 8192
)
