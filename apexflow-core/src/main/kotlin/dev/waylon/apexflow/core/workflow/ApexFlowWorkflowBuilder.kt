package dev.waylon.apexflow.core.workflow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Builder for creating ApexFlow workflow engines with a fluent API
 *
 * @param I Input data type
 * @param O Output data type
 */
class ApexFlowWorkflowBuilder<I, O> {
    private lateinit var _reader: WorkflowReader<I>
    private lateinit var _processor: WorkflowProcessor<I, O>
    private lateinit var _writer: WorkflowWriter<O>

    // Mutable configuration properties - public for DSL usage
    var readBufferSize: Int = 100
    var processBufferSize: Int = 100
    var errorHandler: (Throwable) -> Unit = { it.printStackTrace() }
    var readDispatcher: CoroutineDispatcher = Dispatchers.IO
    var processDispatcher: CoroutineDispatcher = Dispatchers.Default
    var writeDispatcher: CoroutineDispatcher = Dispatchers.IO
    var ioBufferSize: Int = 4 * 8192

    /**
     * Set the workflow reader
     */
    fun reader(reader: WorkflowReader<I>) {
        this._reader = reader
    }

    /**
     * Set the workflow processor
     */
    fun processor(processor: WorkflowProcessor<I, O>) {
        this._processor = processor
    }

    /**
     * Set the workflow writer
     */
    fun writer(writer: WorkflowWriter<O>) {
        this._writer = writer
    }

    /**
     * Configure the workflow with a DSL
     */
    fun configure(block: ApexFlowWorkflowBuilder<I, O>.() -> Unit) {
        this.apply(block)
    }

    /**
     * Build the workflow engine
     */
    fun build(): ApexFlowWorkflowEngine<I, O> {
        require(::_reader.isInitialized) { "Reader must be set" }
        require(::_processor.isInitialized) { "Processor must be set" }
        require(::_writer.isInitialized) { "Writer must be set" }

        // Create config from mutable properties
        val config = WorkflowConfig(
            readBufferSize = readBufferSize,
            processBufferSize = processBufferSize,
            errorHandler = errorHandler,
            readDispatcher = readDispatcher,
            processDispatcher = processDispatcher,
            writeDispatcher = writeDispatcher,
            ioBufferSize = ioBufferSize
        )

        return ApexFlowWorkflowEngine(_reader, _processor, _writer).also {
            it.configure(config)
        }
    }
}

/**
 * DSL function to create an ApexFlow workflow engine
 *
 * Example usage:
 * ```kotlin
 * // For different input and output types
 * val engine = apexFlowWorkflow<InputType, OutputType> {
 *     reader(MyReader())
 *     processor(MyProcessor())
 *     writer(MyWriter())
 *     configure {
 *         readBufferSize = 8192
 *     }
 * }
 *
 * // For same input and output type
 * val engine = apexFlowWorkflow<BufferedImage, BufferedImage> {
 *     reader(TiffReader(inputPath))
 *     processor(WorkflowProcessor.noOp())
 *     writer(PdfWriter(outputPath))
 * }
 * ```
 */
fun <I, O> apexFlowWorkflow(block: ApexFlowWorkflowBuilder<I, O>.() -> Unit): ApexFlowWorkflowEngine<I, O> {
    return ApexFlowWorkflowBuilder<I, O>().apply(block).build()
}
