package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowWriter
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts

/**
 * PDF text writer configuration DSL
 */
class PdfTextWriterConfig {
    var fontSize: Float = 12f
    var margin: Float = 50f
    var pageSize: PDRectangle = PDRectangle.LETTER
    var font: Standard14Fonts.FontName = Standard14Fonts.FontName.HELVETICA
}

/**
 * PDF text writer implementation using PDFBox library
 *
 * Only supports writing to OutputStream
 */
class PdfTextWriter(
    private val outputStream: OutputStream,
    private val config: PdfTextWriterConfig.() -> Unit = {}
) : WorkflowWriter<String> {

    private val pdfConfig = PdfTextWriterConfig().apply(config)

    fun configure(config: PdfTextWriterConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    override suspend fun write(data: Flow<String>) {
        PDDocument().use { document ->
            data.collect { text ->
                // Create new page
                val page = PDPage(pdfConfig.pageSize)
                document.addPage(page)

                // Create content stream for writing text
                PDPageContentStream(document, page).use { contentStream ->
                    // Create PDType1Font from font name enum
                    val pdFont = PDType1Font(pdfConfig.font)

                    // Set font and font size
                    contentStream.setFont(pdFont, pdfConfig.fontSize)

                    // Calculate text position (margin from edges)
                    val startX = pdfConfig.margin
                    val startY = page.mediaBox.height - pdfConfig.margin

                    // Write text to PDF
                    contentStream.beginText()
                    contentStream.newLineAtOffset(startX, startY)
                    contentStream.showText(text)
                    contentStream.endText()
                }
            }
            // Save the document to the output stream
            document.save(outputStream)
            outputStream.flush()
        }
    }
}
