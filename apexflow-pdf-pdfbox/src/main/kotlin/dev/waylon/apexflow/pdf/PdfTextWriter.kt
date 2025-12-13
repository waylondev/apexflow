package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.FileWorkflowWriter
import java.awt.Color
import kotlinx.coroutines.flow.Flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font

/**
 * PDF text writer configuration DSL
 *
 * Provides a minimal API for configuring PDF text writer
 * Most formatting options are left to client implementation
 */
class PdfTextWriterConfig {
    /** Whether to add new page for each text item, default is false */
    var newPagePerItem: Boolean = false
}

/**
 * Minimal PDF text writer implementation using PDFBox library
 *
 * Provides basic text writing functionality with minimal configuration
 * For advanced formatting, client should implement their own WorkflowWriter
 *
 * DSL usage example:
 * ```kotlin
 * val textWriter = PdfTextWriter(outputPath) {
 *     newPagePerItem = false
 * }
 * ```
 *
 * For advanced customization, client can implement their own WorkflowWriter:
 * ```kotlin
 * class CustomPdfTextWriter(outputPath: String) : FileWorkflowWriter<String> {
 *     override fun setOutput(filePath: String) { /* implementation */ }
 *     override suspend fun write(data: Flow<String>) {
 *         // Full control over PDFBox API here
 *     }
 * }
 * ```
 */
class PdfTextWriter(
    // Optional output path to set during construction
    private var outputPath: String,
    // Optional configuration block using DSL
    config: PdfTextWriterConfig.() -> Unit = {}
) : FileWorkflowWriter<String> {
    
    // Configuration instance
    private val textConfig = PdfTextWriterConfig().apply(config)

    /**
     * Set the output PDF file path
     *
     * @param filePath Path to the output PDF file
     */
    override fun setOutput(filePath: String) {
        this.outputPath = filePath
    }
    
    /**
     * Configure PDF text writer using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfTextWriterConfig.() -> Unit) {
        textConfig.apply(config)
    }

    /**
     * Write text content to PDF file
     *
     * @param data Flow of String to write
     */
    override suspend fun write(data: Flow<String>) {
        // Create new PDF document
        PDDocument().use { document ->
            // Process each text item
            data.collect { text ->
                // Create new page with PDFBox default settings
                // No hardcoded page size - uses PDFBox default
                val page = PDPage()
                document.addPage(page)
                
                // Create content stream for new page
                PDPageContentStream(document, page).use { contentStream ->
                    // Write raw text without any formatting
                    // No hardcoded margins, line spacing, or font settings
                    contentStream.beginText()
                    contentStream.newLineAtOffset(0f, page.mediaBox.height)
                    contentStream.showText(text)
                    contentStream.endText()
                }
            }
            
            // Save PDF document
            document.save(outputPath)
        }
    }
}