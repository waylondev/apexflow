package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowWriter
import java.awt.image.BufferedImage
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow

/**
 * PDF image writer configuration DSL
 */
class PdfImageWriterConfig {
    var jpegQuality: Float = 85f
}

/**
 * PDF image writer implementation using PDFBox library
 *
 * Only supports writing to OutputStream
 */
class PdfImageWriter(
    private val outputStream: OutputStream,
    private val config: PdfImageWriterConfig.() -> Unit = {}
) : WorkflowWriter<BufferedImage> {
    
    private val pdfConfig = PdfImageWriterConfig().apply(config)

    fun configure(config: PdfImageWriterConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    override suspend fun write(data: Flow<BufferedImage>) {
        // PDF writing implementation will be added later
        // For now, just write to OutputStream
        outputStream.write("PDF content".toByteArray())
        outputStream.flush()
    }
}
