package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowWriter
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font

/**
 * PDF text writer configuration DSL
 *
 * Provides a fluent API for configuring PDF text writer
 */
class PdfTextWriterConfig {
    var pageSize: PDRectangle = PDRectangle.LETTER
    var fontSize: Float = 12f
    var margin: Float = 50f
}

/**
 * PDF text writer implementation using PDFBox library
 *
 * Supports writing to OutputStream with configurable options
 * Writes text to PDF files
 *
 * DSL usage example:
 * ```kotlin
 * val writer = PdfTextWriter(outputStream) {
 *     fontSize = 14f
 *     margin = 40f
 * }
 * ```
 */
class PdfTextWriter(
    private val outputStream: OutputStream,
    private val config: PdfTextWriterConfig.() -> Unit = {}
) : WorkflowWriter<String> {
    
    // Configuration instance
    private val pdfConfig = PdfTextWriterConfig().apply(config)

    /**
     * Configure PDF text writer using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfTextWriterConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    /**
     * Write text flow to PDF OutputStream
     *
     * @param data Flow of strings to write
     */
    override suspend fun write(data: Flow<String>) {
        // Create PDF document
        PDDocument().use { document ->
            // Collect text from flow and write to PDF
            data.collect { text ->
                // Create new page
                val page = PDPage(pdfConfig.pageSize)
                document.addPage(page)

                // Create content stream for writing text
                PDPageContentStream(document, page).use { contentStream ->
                    // Set font and font size
                    contentStream.setFont(PDType1Font.HELVETICA, pdfConfig.fontSize)
                    
                    // Calculate text position (margin from edges)
                    val startX = pdfConfig.margin
                    val startY = page.mediabox.height - pdfConfig.margin
                    
                    // Write text to PDF
                    contentStream.beginText()
                    contentStream.newLineAtOffset(startX, startY)
                    contentStream.showText(text)
                    contentStream.endText()
                }
            }

            // Save PDF to OutputStream
            document.save(outputStream)
            outputStream.flush()
        }
    }
}
