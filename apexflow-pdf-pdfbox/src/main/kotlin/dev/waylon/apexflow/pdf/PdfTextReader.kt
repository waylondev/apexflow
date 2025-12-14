package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowReader
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

/**
 * PDF text reader configuration DSL
 *
 * Provides a fluent API for configuring PDF text reader
 */
class PdfTextReaderConfig {
    var startPage: Int = 1
    var endPage: Int = Int.MAX_VALUE
}

/**
 * PDF text reader implementation using PDFBox library
 *
 * Supports reading from InputStream with configurable options
 * Reads text from PDF files
 *
 * DSL usage example:
 * ```kotlin
 * val reader = PdfTextReader(inputStream) {
 *     startPage = 1
 *     endPage = 5
 * }
 * ```
 */
class PdfTextReader(
    private val inputStream: InputStream,
    private val config: PdfTextReaderConfig.() -> Unit = {}
) : WorkflowReader<String> {
    
    // Configuration instance
    private val pdfConfig = PdfTextReaderConfig().apply(config)

    /**
     * Configure PDF text reader using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfTextReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    /**
     * Read text from PDF InputStream and return a Flow of strings
     *
     * @return Flow<String> Flow of text strings from PDF pages
     */
    override fun read(): Flow<String> = flow { // Explicit type declaration
        // Create PDDocument from the provided InputStream
        PDDocument.load(inputStream).use { document ->
            // Create PDFTextStripper
            val textStripper = PDFTextStripper()
            textStripper.startPage = pdfConfig.startPage
            textStripper.endPage = minOf(pdfConfig.endPage, document.numberOfPages)

            // Extract text from PDF
            val text = textStripper.getText(document)
            emit(text)
        }
    }
}
