package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import org.slf4j.LoggerFactory

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

    // Logger for debug-level monitoring with lazy initialization
    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    private var config: WorkflowConfig = WorkflowConfig()

    override fun configure(config: WorkflowConfig) {
        this.config = config
    }

    override suspend fun startAsync() {
        // High-performance parallel data flow with dedicated coroutines for each stage
        // Modern Kotlin: Use Flow operators for optimal parallelism

        // Lazy log initialization - only check if debug is enabled when needed
        if (logger.isDebugEnabled) {
            logger.debug("Starting workflow execution...")
            logger.debug(
                "Workflow config: readBufferSize={}, processBufferSize={}, ioBufferSize={}",
                config.readBufferSize, config.processBufferSize, config.ioBufferSize
            )
        }

        // Minimal overhead path without performance monitoring
        runCatching {
            if (logger.isDebugEnabled) {
                logger.debug("Starting Step 1: Reading data with dispatcher: {}", config.readDispatcher)
            }

            // Step 1: Read data
            val dataFlow = reader.read()
                .buffer(config.readBufferSize)  // Read buffer for balancing reader and processor
                .flowOn(config.readDispatcher)

            if (logger.isDebugEnabled) {
                logger.debug("Starting Step 2: Processing data with dispatcher: {}", config.processDispatcher)
            }

            // Step 2: Process data
            val processedFlow = processor.process(dataFlow)
                .buffer(config.processBufferSize)  // Process buffer for balancing processor and writer
                .flowOn(config.processDispatcher)

            if (logger.isDebugEnabled) {
                logger.debug("Starting Step 3: Writing data with dispatcher: {}", config.writeDispatcher)
            }

            // Step 3: Write data
            writer.write(processedFlow.flowOn(config.writeDispatcher))

            if (logger.isDebugEnabled) {
                logger.debug("Workflow execution completed successfully")
            }
        }.onFailure {
            // Handle and rethrow errors
            logger.error("Workflow execution failed with error: {}", it.message, it)
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
