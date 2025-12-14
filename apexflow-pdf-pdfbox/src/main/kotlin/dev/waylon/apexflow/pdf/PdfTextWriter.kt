package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowWriter
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow

/**
 * PDF text writer configuration DSL
 */
class PdfTextWriterConfig {
    var fontSize: Float = 12f
    var margin: Float = 50f
}

/**
 * PDF text writer implementation using PDFBox library
 *
 * Only supports writing to OutputStream
 */
class PdfTextWriter(
    private val outputStream: OutputStream,
    private val config: PdfTextWriterConfig.() -> Unit = {}
) : WorkflowWriter<String> {
    
    private val pdfConfig = PdfTextWriterConfig().apply(config)

    fun configure(config: PdfTextWriterConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    override suspend fun write(data: Flow<String>) {
        // PDF text writing implementation will be added later
        // For now, just write to OutputStream
        data.collect {
            outputStream.write(it.toByteArray())
        }
        outputStream.flush()
    }
}
