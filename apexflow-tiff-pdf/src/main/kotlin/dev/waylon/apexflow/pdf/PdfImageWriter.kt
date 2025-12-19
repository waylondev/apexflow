package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.image.ApexImageWriter
import dev.waylon.apexflow.image.ImageConstants
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory

/**
 * PDF image writer configuration
 */
class PdfImageWriterConfig {
    /**
     * JPEG compression quality (0-100)
     * Higher values result in better quality but larger file sizes
     */
    var jpegQuality: Float = ImageConstants.DEFAULT_JPEG_QUALITY

    /**
     * Whether to compress PDF content
     */
    var compressContent: Boolean = true

    /**
     * PDF version to use (e.g., "1.7", "2.0")
     */
    var pdfVersion: String = ImageConstants.DEFAULT_PDF_VERSION

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
        var creator: String = ImageConstants.DEFAULT_PDF_CREATOR
        var producer: String = ImageConstants.DEFAULT_PDF_PRODUCER
    }
}

/**
 * PDF image writer implementation using PDFBox library
 *
 * Supports writing to OutputStream with configurable options
 */
class PdfImageWriter @JvmOverloads constructor(
    private val outputStream: OutputStream,
    private val config: PdfImageWriterConfig = PdfImageWriterConfig()
) : ApexImageWriter {
    /**
     * Convenience constructor: File + configuration
     */
    @JvmOverloads
    constructor(
        file: File,
        config: PdfImageWriterConfig = PdfImageWriterConfig()
    ) : this(file.outputStream(), config)

    private val logger = createLogger<PdfImageWriter>()


    /**
     * Write BufferedImage flow to PDF OutputStream
     *
     * Each BufferedImage is written as a separate page in the PDF document
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
        logger.info("Starting PDF writing process with JPEG quality: {}", config.jpegQuality)

        val quality = config.jpegQuality / 100f
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

            logger.debug("Adding {} pages to PDF document", pageIndex)
            // Save the document to the output stream
            logger.debug("Saving PDF document to output stream")
            document.save(outputStream)
            outputStream.flush()
            logger.debug("PDF document saved successfully")
        }

        logger.info("Completed PDF writing process successfully")

    }
}

/**
 * Extension function: Convert OutputStream to PdfImageWriter with lambda configuration
 *
 * @param config Lambda function to configure PDF writer settings
 * @return PdfImageWriter instance with specified configuration
 */
fun OutputStream.toPdfImageWriter(config: PdfImageWriterConfig.() -> Unit = {}): PdfImageWriter {
    return PdfImageWriter(this, PdfImageWriterConfig().apply(config))
}

/**
 * Extension function: Convert File to PdfImageWriter with lambda configuration
 *
 * @param config Lambda function to configure PDF writer settings
 * @return PdfImageWriter instance with specified configuration
 */
fun File.toPdfImageWriter(config: PdfImageWriterConfig.() -> Unit = {}): PdfImageWriter {
    return PdfImageWriter(this, PdfImageWriterConfig().apply(config))
}