package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.ApexFlowWriter
import dev.waylon.apexflow.core.util.createLogger
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory

/**
 * ApexFlow PDF writer component
 *
 * Flow<BufferedImage> -> Flow<Unit>
 *
 * Direct implementation of ApexFlowWriter interface with complete PDF writing logic
 * Optimized to write pages as images are received, not after all images are collected
 */
class ApexPdfWriter private constructor(
    private val outputProvider: () -> OutputStream,
    private val config: PdfConfig = PdfConfig()
) : ApexFlowWriter<BufferedImage> {

    companion object {
        /**
         * Create a PDF writer to file path
         */
        fun toPath(filePath: String, config: PdfConfig = PdfConfig()): ApexPdfWriter {
            return ApexPdfWriter({ File(filePath).outputStream() }, config)
        }

        /**
         * Create a PDF writer to file
         */
        fun toFile(file: File, config: PdfConfig = PdfConfig()): ApexPdfWriter {
            return ApexPdfWriter({ file.outputStream() }, config)
        }

        /**
         * Create a PDF writer to output stream
         */
        fun toOutputStream(outputStream: OutputStream, config: PdfConfig = PdfConfig()): ApexPdfWriter {
            return ApexPdfWriter({ outputStream }, config)
        }
    }

    private val logger = createLogger<ApexPdfWriter>()

    override fun toFile(file: File): ApexFlowWriter<BufferedImage> {
        return toFile(file, config)
    }

    override fun toOutputStream(outputStream: OutputStream): ApexFlowWriter<BufferedImage> {
        return toOutputStream(outputStream, config)
    }

    override fun toPath(filePath: String): ApexFlowWriter<BufferedImage> {
        return toPath(filePath, config)
    }

    override fun transform(input: Flow<BufferedImage>): Flow<Unit> {
        return flow {
            logger.info("ApexFlow PDF writing started")

            // Use use() for automatic resource management
            outputProvider().use { outputStream ->
                PDDocument().use { document ->
                    var pageIndex = 0

                    // Write pages as images are received - no need to collect all images first!
                    input.collect { image ->
                        logger.debug("Writing PDF page {}", pageIndex)

                        // Create and add page
                        val page = PDPage(config.pageSize)
                        document.addPage(page)

                        // Write content to page using use() for automatic resource management
                        PDPageContentStream(document, page).use { contentStream ->
                            val pdImage = JPEGFactory.createFromImage(document, image, config.jpegQuality)
                            contentStream.drawImage(pdImage, 0f, 0f, page.mediaBox.width, page.mediaBox.height)
                        }

                        pageIndex++
                        emit(Unit) // Signal completion for this page
                    }

                    // Save the complete document once all pages are added
                    logger.info("Saving PDF document with $pageIndex pages")
                    document.save(outputStream)

                    logger.info("ApexFlow PDF writing completed, wrote $pageIndex pages")
                }
            }
        }
    }
}