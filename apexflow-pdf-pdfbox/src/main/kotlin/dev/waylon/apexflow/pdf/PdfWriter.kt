package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.FileWorkflowWriter
import java.awt.image.BufferedImage
import kotlinx.coroutines.flow.Flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.slf4j.LoggerFactory

/**
 * PDF writer implementation using PDFBox library
 *
 * Implements FileWorkflowWriter interface for writing BufferedImage to PDF files
 * Optimized for high performance
 */
class PdfWriter(
    // Optional output path to set during construction
    private var outputPath: String,
    // JPEG quality (0-100), default 85 for good balance of quality and size
    private val jpegQuality: Float = 85f
) : FileWorkflowWriter<BufferedImage> {

    // Lazy logger initialization for better startup performance
    private val logger by lazy { LoggerFactory.getLogger(PdfWriter::class.java) }

    /**
     * Set the output PDF file path
     *
     * @param filePath Path to the output PDF file
     */
    override fun setOutput(filePath: String) {
        this.outputPath = filePath
        if (logger.isDebugEnabled) {
            logger.debug("Set PDF output file: {}", filePath)
        }
    }

    /**
     * Write BufferedImage flow to PDF file
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
        val path = outputPath

        if (logger.isInfoEnabled) {
            logger.info("Starting PDF generation: {}", path)
        }

        // Create PDF document
        PDDocument().use { document ->
            // Collect all images from the flow and add to PDF
            data.collect { image ->
                // Create page with the same size as the image
                val page = PDPage(PDRectangle(image.width.toFloat(), image.height.toFloat()))
                document.addPage(page)

                // Create PDImageXObject from BufferedImage with JPEG compression for smaller file size
                val pdImage = JPEGFactory.createFromImage(document, image, jpegQuality / 100f)

                // Write image to PDF page
                PDPageContentStream(document, page).use { contentStream ->
                    contentStream.drawImage(pdImage, 0f, 0f)
                }

                if (logger.isDebugEnabled) {
                    logger.debug("Added image to PDF: {}x{}, JPEG quality: {}", image.width, image.height, jpegQuality)
                }
            }

            // Save PDF document
            document.save(path)

            if (logger.isInfoEnabled) {
                logger.info("PDF generation completed: {}, pages: {}", path, document.numberOfPages)
            }
        }
    }
}