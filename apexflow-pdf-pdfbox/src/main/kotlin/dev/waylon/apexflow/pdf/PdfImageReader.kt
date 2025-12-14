package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowReader
import java.awt.image.BufferedImage
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * PDF image reader configuration DSL
 */
class PdfImageReaderConfig {
    var dpi: Int = 300
}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Only supports reading from InputStream
 */
class PdfImageReader(
    private val inputStream: InputStream,
    private val config: PdfImageReaderConfig.() -> Unit = {}
) : WorkflowReader<BufferedImage> {
    
    private val pdfConfig = PdfImageReaderConfig().apply(config)

    fun configure(config: PdfImageReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    override fun read(): Flow<BufferedImage> = flow { 
        // PDF reading implementation will be added later
        // For now, just emit a dummy image
        emit(BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
    }
}
