package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowWriter
import java.awt.image.BufferedImage
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory

/**
 * PDF image writer configuration DSL
 *
 * Provides a fluent API for configuring PDF image writer
 */
class PdfImageWriterConfig {
    var jpegQuality: Float = 85f
}

/**
 * PDF image writer implementation using PDFBox library
 *
 * Supports writing to OutputStream with configurable options
 * Writes BufferedImage to PDF files
 *
 * DSL usage example:
 * ```kotlin
 * val writer = PdfImageWriter(outputStream) {
 *     jpegQuality = 90f
 * }
 * ```
 */
class PdfImageWriter(
    private val outputStream: OutputStream,
    private val config: PdfImageWriterConfig.() -> Unit = {}
) : WorkflowWriter<BufferedImage> {
    
    // Configuration instance
    private val pdfConfig = PdfImageWriterConfig().apply(config)

    /**
     * Configure PDF writer using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfImageWriterConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    /**
     * Get the current JPEG quality setting
     *
     * @return Float JPEG quality as percentage (0-100)
     */
    fun getJpegQuality(): Float {
        return pdfConfig.jpegQuality
    }

    /**
     * Set custom JPEG quality for PDF writing
     *
     * @param quality JPEG quality as percentage (0-100)
     */
    fun setJpegQuality(quality: Float) {
        pdfConfig.jpegQuality = quality
    }

    /**
     * Write BufferedImage flow to PDF OutputStream
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
        // Create PDF document
        PDDocument().use { document ->
            // Collect all images from the flow and add to PDF
            data.collect { image ->
                // Create page with the same size as the image
                val page = PDPage(PDRectangle(image.width.toFloat(), image.height.toFloat()))
                document.addPage(page)

                // Create PDImageXObject from BufferedImage with JPEG compression for smaller file size
                val pdImage = JPEGFactory.createFromImage(document, image, pdfConfig.jpegQuality / 100f)

                // Write image to PDF page
                PDPageContentStream(document, page).use { contentStream ->
                    contentStream.drawImage(pdImage, 0f, 0f)
                }
                
                // 🔧 MEMORY OPTIMIZATION: Release resources immediately after use
                image.flush()
            }

            // Save PDF document to OutputStream
            document.save(outputStream)
            // Flush to ensure all data is written
            outputStream.flush()
        }
    }
}
