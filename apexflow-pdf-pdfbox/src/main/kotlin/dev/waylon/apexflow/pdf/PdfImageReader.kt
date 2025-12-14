package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowReader
import java.awt.image.BufferedImage
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.pdmodel.PDDocument

/**
 * PDF image reader configuration DSL
 *
 * Provides a fluent API for configuring PDF image reader
 */
class PdfImageReaderConfig {
    var dpi: Int = 300
}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Supports reading from InputStream with configurable options
 * Reads PDF files and converts pages to BufferedImage
 *
 * DSL usage example:
 * ```kotlin
 * val reader = PdfImageReader(inputStream) {
 *     dpi = 200
 * }
 * ```
 */
class PdfImageReader(
    private val inputStream: InputStream,
    private val config: PdfImageReaderConfig.() -> Unit = {}
) : WorkflowReader<BufferedImage> {
    
    // Configuration instance
    private val pdfConfig = PdfImageReaderConfig().apply(config)

    /**
     * Configure PDF reader using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfImageReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    /**
     * Read PDF from InputStream and return a Flow of BufferedImage
     *
     * For PDF files, returns each page as a separate BufferedImage
     *
     * @return Flow<BufferedImage> Flow of images from the PDF file
     */
    override fun read(): Flow<BufferedImage> = flow { // Explicit type declaration
        // Create PDDocument from the provided InputStream
        PDDocument.load(inputStream).use { document ->
            // Get number of pages
            val numPages = document.numberOfPages

            // Read each page
            for (pageIndex in 0 until numPages) {
                val page = document.getPage(pageIndex)
                
                // Convert PDF page to BufferedImage with specified DPI
                val image = page.convertToImage(java.awt.image.BufferedImage.TYPE_INT_RGB, pdfConfig.dpi)
                emit(image)
            }
        }
    }
}
