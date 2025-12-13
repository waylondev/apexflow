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
 * Render quality strategy enum
 * Defines different rendering quality levels for PDF pages
 */
enum class RenderQuality {
    /** High quality rendering with full color and high DPI */
    HIGH,

    /** Balanced quality rendering with good color and medium DPI */
    BALANCED,

    /** Fast rendering with grayscale and low DPI */
    FAST,

    /** Ultra fast rendering with minimal processing */
    ULTRA_FAST
}

/**
 * PDF reader implementation using PDFBox library
 *
 * Implements FileWorkflowReader interface for reading PDF files
 * Supports reading multi-page PDF files and converting each page to BufferedImage
 * Optimized for high performance with configurable rendering strategies
 */
class PdfReader(
    // Optional input path to set during construction
    private var inputPath: String,
    // DPI for rendering PDF pages, default 100 for better performance
    private var dpi: Float = 100f,
    // Render quality strategy, default to balanced for good performance and quality
    private var renderQuality: RenderQuality = RenderQuality.HIGH
) : FileWorkflowReader<BufferedImage> {



    /**
     * Set the input PDF file path
     *
     * @param filePath Path to the PDF file
     */
    override fun setInput(filePath: String) {
        this.inputPath = filePath
    }

    /**
     * Set DPI for rendering PDF pages
     *
     * @param dpi Dots per inch for rendering
     */
    fun setDpi(dpi: Float) {
        // Validate DPI value
        this.dpi = dpi
    }

    /**
     * Set render quality strategy
     *
     * @param quality Render quality strategy to use
     */
    fun setRenderQuality(quality: RenderQuality) {
        this.renderQuality = quality
    }

    /**
     * Get the effective DPI based on render quality
     */
    private fun getEffectiveDpi(): Float {
        return when (renderQuality) {
            RenderQuality.HIGH -> maxOf(dpi, 300f)
            RenderQuality.BALANCED -> dpi
            RenderQuality.FAST -> maxOf(dpi / 2, 50f)
            RenderQuality.ULTRA_FAST -> maxOf(dpi / 4, 25f)
        }
    }

    /**
     * Convert image to grayscale if needed based on render quality
     */
    private fun processImageForQuality(image: BufferedImage): BufferedImage {
        // Skip processing for high quality
        if (renderQuality == RenderQuality.HIGH) {
            return image
        }

        // Convert to grayscale for fast and ultra-fast modes
        if (renderQuality == RenderQuality.FAST || renderQuality == RenderQuality.ULTRA_FAST) {
            val grayImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
            val g2d = grayImage.createGraphics()
            g2d.drawImage(image, 0, 0, null)
            g2d.dispose()
            // Flush original image to free memory
            image.flush()
            return grayImage
        }

        // For balanced mode, return as is
        return image
    }

    /**
     * Apply resizing for ultra-fast mode
     */
    private fun resizeImageForUltraFast(image: BufferedImage): BufferedImage {
        val scale = 0.5f // Reduce size by half for ultra-fast mode
        val newWidth = (image.width * scale).toInt()
        val newHeight = (image.height * scale).toInt()

        val resizedImage = BufferedImage(newWidth, newHeight, image.type)
        val g2d = resizedImage.createGraphics()
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null)
        g2d.dispose()
        // Flush original image to free memory
        image.flush()
        return resizedImage
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


                // Get effective DPI based on render quality
                val effectiveDpi = getEffectiveDpi()



                // Render the current page with effective DPI
                var renderedImage = renderer.renderImageWithDPI(pageIndex, effectiveDpi)

                // Process image based on render quality
                renderedImage = processImageForQuality(renderedImage)

                // Apply additional optimizations for ultra-fast mode
                if (renderQuality == RenderQuality.ULTRA_FAST) {
                    renderedImage = resizeImageForUltraFast(renderedImage)
                }



                // Emit the final image
                emit(renderedImage)

                // Flush the image from memory after emission
                renderedImage.flush()
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
