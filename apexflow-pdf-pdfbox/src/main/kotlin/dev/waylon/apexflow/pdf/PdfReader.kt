package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.FileWorkflowReader
import java.awt.image.BufferedImage
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer


/**
 * PDF reader configuration DSL
 *
 * Provides a fluent API for configuring PDF reader
 * No default values - client must explicitly set all required parameters
 */
class PdfReaderConfig {
    var dpi: Float? = null
    var imageProcessor: ((BufferedImage) -> BufferedImage)? = null
}

/**
 * PDF reader implementation using PDFBox library
 *
 * Implements FileWorkflowReader interface for reading PDF files
 * Supports reading multi-page PDF files and converting each page to BufferedImage
 * Optimized for high performance with configurable rendering strategies
 *
 * DSL usage example:
 * ```kotlin
 * val reader = PdfReader(inputPath) {
 *     dpi = 200f
 *     imageProcessor {
 *         // Custom image processing logic
 *         val grayImage = BufferedImage(it.width, it.height, BufferedImage.TYPE_BYTE_GRAY)
 *         val g2d = grayImage.createGraphics()
 *         g2d.drawImage(it, 0, 0, null)
 *         g2d.dispose()
 *         grayImage
 *     }
 * }
 * ```
 */
class PdfReader(
    // Optional input path to set during construction
    private var inputPath: String,
    // Optional configuration block using DSL
    config: PdfReaderConfig.() -> Unit = {}
) : FileWorkflowReader<BufferedImage> {

    // Configuration instance
    private val pdfConfig = PdfReaderConfig().apply(config)

    /**
     * Set the input PDF file path
     *
     * @param filePath Path to the PDF file
     */
    override fun setInput(filePath: String) {
        this.inputPath = filePath
    }

    /**
     * Configure PDF reader using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: PdfReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    /**
     * Set DPI for rendering PDF pages (traditional API for backward compatibility)
     *
     * @param dpi Dots per inch for rendering
     */
    fun setDpi(dpi: Float) {
        pdfConfig.dpi = dpi
    }

    /**
     * Get current DPI setting
     *
     * @return Current DPI value, null if not set
     */
    fun getDpi(): Float? {
        return pdfConfig.dpi
    }

    /**
     * Set custom image processor (traditional API for backward compatibility)
     *
     * @param processor Custom image processor function
     */
    fun setImageProcessor(processor: ((BufferedImage) -> BufferedImage)?) {
        pdfConfig.imageProcessor = processor
    }


    /**
     * Read PDF file and return a Flow of BufferedImage
     *
     * @return Flow<BufferedImage> Flow of images from the PDF file
     *
     * This implementation uses efficient single-threaded rendering with:
     * - True streaming behavior: render one page, emit one page
     * - Memory management: flush images after emission
     * - Render quality strategies: support for different rendering qualities
     * - Grayscale rendering: faster rendering for certain use cases
     * - Resource efficiency: minimal memory usage
     */
    override fun read(): Flow<BufferedImage> = flow {
        // Step 1: Validate input path and get File object
        val file = validateInput()
        val path = file.absolutePath



        // Step 2: Open and load PDF document with automatic resource management
        Loader.loadPDF(file).use { document ->
            // Step 3: Create PDF renderer
            val renderer = PDFRenderer(document)

            // Step 4: Get page count
            val pageCount = document.numberOfPages



            // Step 5: Render pages sequentially with rendering strategy optimization
            repeat(pageCount) { pageIndex ->


                // Verify client has set required DPI parameter
                val clientDpi = pdfConfig.dpi ?: throw IllegalStateException("DPI must be explicitly set by client")
                
                // Render the current page with the exact DPI provided by client
                var renderedImage = renderer.renderImageWithDPI(pageIndex, clientDpi)

                // Apply custom processor if provided
                val processedImage = pdfConfig.imageProcessor?.invoke(renderedImage) ?: renderedImage
                
                // If a new image was created, flush the original
                if (processedImage !== renderedImage) {
                    renderedImage.flush()
                }

                // Emit the final image
                emit(processedImage)

                // Flush the image from memory after emission
                processedImage.flush()
            }


        }
    }.catch {
        // Centralized error handling
        throw it
    }

    /**
     * Validate input path and return File object
     *
     * @return File Validated file object
     */
    private fun validateInput(): File {
        // Step 1: Validate input path is set
        val path = inputPath



        // Step 2: Validate file exists and is a file
        return File(path).also {
            require(it.exists()) { "PDF file does not exist: $path" }
            require(it.isFile) { "Path is not a file: $path" }
        }
    }
}
