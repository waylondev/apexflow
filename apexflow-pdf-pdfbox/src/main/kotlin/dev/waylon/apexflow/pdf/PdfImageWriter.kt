package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.FileWorkflowWriter
import java.awt.image.BufferedImage
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
 * Writes BufferedImage to PDF files
 * Supports writing with configurable JPEG quality
 *
 * DSL usage example:
 * ```kotlin
 * val writer = PdfImageWriter(outputPath) {
 *     jpegQuality = 90f
 * }
 * ```
 */
class PdfImageWriter(
    // Optional output path to set during construction
    private var outputPath: String,
    // Optional configuration block using DSL
    config: PdfImageWriterConfig.() -> Unit = {}
) : FileWorkflowWriter<BufferedImage> {
    
    // Configuration instance
    private val pdfConfig = PdfImageWriterConfig().apply(config)

    /**
     * Set the output PDF file path
     *
     * @param filePath Path to the output PDF file
     */
    override fun setOutput(filePath: String) {
        this.outputPath = filePath
    }
    
    /**
     * Configure PDF image writer using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfImageWriterConfig.() -> Unit) {
        pdfConfig.apply(config)
    }
    
    /**
     * Set JPEG quality for image compression
     *
     * @param quality JPEG quality (0-100), higher means better quality but larger file size
     */
    fun setJpegQuality(quality: Float) {
        this.pdfConfig.jpegQuality = quality.coerceIn(0f, 100f)
    }
    
    /**
     * Get current JPEG quality setting
     *
     * @return Current JPEG quality (0-100)
     */
    fun getJpegQuality(): Float {
        return pdfConfig.jpegQuality
    }

    /**
     * Write BufferedImage flow to PDF file
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
        val path = outputPath

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
                // Flush the original image from memory immediately after use
                image.flush()
            }

            // Save PDF document
            document.save(path)
        }
    }
}