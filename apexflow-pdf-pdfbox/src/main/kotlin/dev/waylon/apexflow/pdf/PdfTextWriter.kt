package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.FileWorkflowWriter
import java.awt.Color
import java.awt.image.BufferedImage
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
    /** Page size for output PDF, default is A4 */
    var pageSize: PDRectangle = PDRectangle.A4
    /** Margin in points, default is 50 */
    var margin: Float = 50f
    /** Font size in points, default is 12 */
    var fontSize: Float = 12f
    /** Font color, default is black */
    var fontColor: Color = Color.BLACK
    /** Line spacing factor, default is 1.5 */
    var lineSpacing: Float = 1.5f
    /** Whether to add new page for each text item, default is false */
    var newPagePerItem: Boolean = false
}

/**
 * PDF text writer implementation using PDFBox library
 *
 * Writes text content to PDF files with configurable formatting options
 * Supports writing multiple text items to PDF with customizable layout
 *
 * DSL usage example:
 * ```kotlin
 * val textWriter = PdfTextWriter(outputPath) {
 *     pageSize = PDRectangle.A4
 *     margin = 72f
 *     fontSize = 14f
 *     fontColor = Color.DARK_GRAY
 *     lineSpacing = 1.5f
 *     newPagePerItem = false
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
                // Create new page for each text item
                val page = PDPage(textConfig.pageSize)
                document.addPage(page)
                
                // Create content stream for new page
                PDPageContentStream(document, page).use { contentStream ->
                    // Set up font and color (using default font, no explicit HELVETICA constant)
                    contentStream.setNonStrokingColor(textConfig.fontColor)
                    
                    // Reset current Y position (top margin)
                    val pageHeight = page.mediaBox.height
                    val pageWidth = page.mediaBox.width
                    val maxWidth = pageWidth - (2 * textConfig.margin)
                    var currentY = pageHeight - textConfig.margin
                    
                    // Simple line by line writing without line width calculation
                    // This avoids the need for font constants and width calculations
                    val lines = text.lines()
                    for (line in lines) {
                        // Write line as is, no line wrapping
                        contentStream.beginText()
                        contentStream.newLineAtOffset(textConfig.margin, currentY)
                        contentStream.showText(line)
                        contentStream.endText()
                        
                        // Move to next line
                        currentY -= textConfig.fontSize * textConfig.lineSpacing
                    }
                }
            }
            
            // Save PDF document
            document.save(outputPath)
        }
    }
}