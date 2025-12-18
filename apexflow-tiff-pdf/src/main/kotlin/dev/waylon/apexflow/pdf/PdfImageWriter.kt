package dev.waylon.apexflow.pdf

import java.awt.image.BufferedImage
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.slf4j.LoggerFactory

/**
 * PDF image writer configuration
 */
class PdfImageWriterConfig {
    /**
     * JPEG compression quality (0-100)
     * Higher values result in better quality but larger file sizes
     */
    var jpegQuality: Float = 85f

    /**
     * Whether to compress PDF content
     */
    var compressContent: Boolean = true

    /**
     * PDF version to use (e.g., "1.7", "2.0")
     */
    var pdfVersion: String = "1.7"

    /**
     * Metadata for the PDF document
     */
    var metadata: PdfMetadata = PdfMetadata()

    /**
     * PDF metadata class
     */
    class PdfMetadata {
        var title: String? = null
        var author: String? = null
        var subject: String? = null
        var keywords: String? = null
        var creator: String = "ApexFlow PDF Writer"
        var producer: String = "Apache PDFBox"
    }
}

/**
 * PDF image writer implementation using PDFBox library
 *
 * Only supports writing to OutputStream
 */
class PdfImageWriter(
    private val outputStream: OutputStream,
    private val config: PdfImageWriterConfig.() -> Unit = {}
) {

    private val logger = LoggerFactory.getLogger(PdfImageWriter::class.java)
    private val pdfConfig = PdfImageWriterConfig().apply(config)

    fun configure(config: PdfImageWriterConfig.() -> Unit) {
        pdfConfig.apply(config)
        logger.debug("Configured PDF writer with JPEG quality: {}", pdfConfig.jpegQuality)
    }

    /**
     * Write BufferedImage flow to PDF OutputStream
     *
     * Each BufferedImage is written as a separate page in the PDF document
     *
     * @param data Flow of BufferedImage to write
     * @throws ConversionWriteException if there's an error writing the PDF data
     */
    suspend fun write(data: Flow<BufferedImage>) {
        logger.info("Starting PDF writing process with JPEG quality: {}", pdfConfig.jpegQuality)

        val quality = pdfConfig.jpegQuality / 100f
        PDDocument().use { document ->
            logger.debug("Created new PDF document")

            var pageIndex = 0
            data.collect { image ->
                pageIndex++
                logger.debug("Adding page {} to PDF document (size: {}x{})", pageIndex, image.width, image.height)

                // Create page with the same size as the image
                val page = PDPage(PDRectangle(image.width.toFloat(), image.height.toFloat()))
                document.addPage(page)

                // Create content stream for writing image
                PDPageContentStream(document, page).use { contentStream ->
                    // Create PDImageXObject from BufferedImage with JPEG compression
                    val pdImage = JPEGFactory.createFromImage(document, image, quality)
                    // Draw image to fit the entire page
                    contentStream.drawImage(pdImage, 0f, 0f)
                }

                logger.debug("Successfully added page {} to PDF document", pageIndex)
            }

            logger.info("Adding {} pages to PDF document", pageIndex)
            // Save the document to the output stream
            logger.debug("Saving PDF document to output stream")
            document.save(outputStream)
            outputStream.flush()
            logger.debug("PDF document saved successfully")
        }

        logger.info("Completed PDF writing process successfully")

    }
}
