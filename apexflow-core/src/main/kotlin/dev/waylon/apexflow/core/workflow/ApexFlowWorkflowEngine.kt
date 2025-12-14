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

    override suspend fun startAsync() {
        // Optimized parallel data flow with minimal context switching
        // Reorder Flow operators for optimal performance
        runCatching {
            // Step 1: Read data - Optimized order: flowOn first, then buffer
            val dataFlow = reader.read()
                .flowOn(config.readDispatcher)  // Move flowOn before buffer for better performance
                .buffer(config.readBufferSize)  // Buffer on the new dispatcher thread

            // Step 2: Process data - Optimized order: flowOn first, then buffer
            val processedFlow = processor.process(dataFlow)
                .flowOn(config.processDispatcher)  // Move flowOn before buffer
                .buffer(config.processBufferSize)  // Buffer on the new dispatcher thread

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
