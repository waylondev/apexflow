package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.FileWorkflowReader
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper

/**
 * PDF text reader configuration DSL
 *
 * Provides a fluent API for configuring PDF text reader
 */
class PdfTextReaderConfig {
    /** Start page for text extraction, default is 1 */
    var startPage: Int = 1
    /** End page for text extraction, null means all pages */
    var endPage: Int? = null
    /** Whether to sort text by position, default is true */
    var sortByPosition: Boolean = true
}

/**
 * PDF text reader implementation using PDFBox library
 *
 * Extracts text content from PDF files
 * Supports extracting text from specific pages with configurable options
 *
 * DSL usage example:
 * ```kotlin
 * val textReader = PdfTextReader(inputPath) {
 *     startPage = 1
 *     endPage = 5
 *     sortByPosition = true
 * }
 * ```
 */
class PdfTextReader(
    // Optional input path to set during construction
    private var inputPath: String,
    // Optional configuration block using DSL
    config: PdfTextReaderConfig.() -> Unit = {}
) : FileWorkflowReader<String> {
    
    // Configuration instance
    private val textConfig = PdfTextReaderConfig().apply(config)

    /**
     * Set the input PDF file path
     *
     * @param filePath Path to the PDF file
     */
    override fun setInput(filePath: String) {
        this.inputPath = filePath
    }
    
    /**
     * Configure PDF text reader using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfTextReaderConfig.() -> Unit) {
        textConfig.apply(config)
    }

    /**
     * Extract text from PDF file and return a Flow of String
     *
     * @return Flow<String> Flow of text content from the PDF file
     */
    override fun read(): Flow<String> = flow {
        // Validate input file
        val file = File(inputPath)
        require(file.exists()) { "PDF file does not exist: $inputPath" }
        require(file.isFile) { "Path is not a file: $inputPath" }

        // Load PDF document
        Loader.loadPDF(file).use { document ->
            // Create PDFTextStripper for text extraction
            val textStripper = PDFTextStripper()
            
            // Configure text stripper with client provided parameters
            textStripper.startPage = textConfig.startPage
            textStripper.endPage = textConfig.endPage ?: document.numberOfPages
            textStripper.sortByPosition = textConfig.sortByPosition
            
            // Extract text content
            val extractedText = textStripper.getText(document)
            
            // Emit the extracted text
            emit(extractedText)
        }
    }
}