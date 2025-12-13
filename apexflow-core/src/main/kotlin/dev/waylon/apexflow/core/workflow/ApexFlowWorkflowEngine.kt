package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn


/**
 * ApexFlow workflow engine implementation
 *
 * High-performance workflow engine based on Kotlin coroutine Flow
 * Implements parallel processing with dedicated coroutines for each stage:
 * 1. Reading (IO dispatcher)
 * 2. Processing (Default dispatcher for CPU-intensive tasks)
 * 3. Writing (IO dispatcher)
 *
 * Uses Flow operators for optimal parallelism and backpressure handling
 * Focused on high-performance conversion: either succeeds or fails
 *
 * @param I Input data type
 * @param O Output data type
 */
class ApexFlowWorkflowEngine<I, O>(
    private val reader: WorkflowReader<I>,
    private val processor: WorkflowProcessor<I, O>,
    private val writer: WorkflowWriter<O>
) : WorkflowEngine {

    private var config: WorkflowConfig = WorkflowConfig()

    override fun configure(config: WorkflowConfig) {
        this.config = config
    }

    /**
     * Configure workflow engine using DSL
     *
     * Example usage:
     * ```kotlin
     * val engine = ApexFlowWorkflowEngine(reader, processor, writer)
     * engine.configure {
     *     readBufferSize = 200
     *     processBufferSize = 200
     *     errorHandler = { e -> println("Error: ${e.message}") }
     * }
     * ```
     */
    fun configure(block: WorkflowConfig.() -> Unit) {
        this.config = WorkflowConfig().apply(block)
    }

    override suspend fun startAsync() {
        // High-performance parallel data flow with dedicated coroutines for each stage
        // Modern Kotlin: Use Flow operators for optimal parallelism

        // Minimal overhead path without logging
        runCatching {
            // Step 1: Read data
            val dataFlow = reader.read()
                .buffer(config.readBufferSize)  // Read buffer for balancing reader and processor
                .flowOn(config.readDispatcher)

            // Step 2: Process data
            val processedFlow = processor.process(dataFlow)
                .buffer(config.processBufferSize)  // Process buffer for balancing processor and writer
                .flowOn(config.processDispatcher)

            // Step 3: Write data
            writer.write(processedFlow.flowOn(config.writeDispatcher))
        }.onFailure {
            // Handle and rethrow errors
            config.errorHandler(it)
            throw it
        }
    }

    // Simplified implementation: No status tracking for high performance
    override fun getStatus(): WorkflowStatus = WorkflowStatus.IDLE

    // Simplified implementation: No stop functionality, focus on high-performance conversion
    override fun stop() {
        // No-op: Focus on high-performance conversion, no support for mid-execution stop
    }
}
