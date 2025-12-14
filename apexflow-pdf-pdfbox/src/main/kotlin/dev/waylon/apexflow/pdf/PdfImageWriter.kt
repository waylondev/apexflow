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
        val quality = pdfConfig.jpegQuality / 100f
        PDDocument().use { document ->
            data.collect { image ->
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
            }
            // Save the document to the output stream
            document.save(outputStream)
            outputStream.flush()
        }
    }
}
