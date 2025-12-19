package dev.waylon.apexflow.pdf

import java.awt.image.BufferedImage
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import org.slf4j.LoggerFactory

/**
 * PDF image reader configuration
 */
class PdfImageReaderConfig {
    /**
     * DPI (dots per inch) for rendering PDF pages
     * Higher values result in better quality but larger file sizes
     */
    var dpi: Float = 150f

    /**
     * Page numbers to render (0-based index)
     * If empty, all pages will be rendered
     */
    var pageNumbers: List<Int> = emptyList()

    /**
     * Whether to skip blank pages during rendering
     */
    var skipBlankPages: Boolean = false

    /**
     * Image type to use for rendering
     */
    var imageType: ImageType = ImageType.RGB

    /**
     * Image type enumeration for PDF rendering
     */
    enum class ImageType {
        /** RGB color space with 8 bits per component */
        RGB,

        /** Grayscale color space with 8 bits per component */
        GRAY,

        /** Binary (black and white) color space */
        BINARY
    }
}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Supports reading from File
 */
class PdfImageReader(
    private val inputFile: File,
    config: PdfImageReaderConfig = PdfImageReaderConfig()
) {

    private val logger = LoggerFactory.getLogger(PdfImageReader::class.java)
    private val config = config

    /**
     * Read PDF pages and return a Flow of BufferedImage
     *
     * Each page is rendered as a separate BufferedImage with the configured DPI
     *
     * @return Flow<BufferedImage> Flow of rendered pages
     */
    fun read(): Flow<BufferedImage> = flow {
        logger.info("Starting PDF reading process with DPI: {}", config.dpi)

        // Load PDF document from file
        Loader.loadPDF(inputFile).use { document ->
            logger.debug("Loaded PDF document successfully")

            val renderer = PDFRenderer(document)
            val pageCount = document.pages.count
            logger.info("Found {} pages in PDF document", pageCount)

            val pagesToRender = if (config.pageNumbers.isEmpty()) {
                0 until pageCount
            } else {
                config.pageNumbers.filter { it in 0 until pageCount }
            }

            pagesToRender.forEach { pageIndex ->
                logger.debug("Rendering PDF page {}/{}", pageIndex + 1, pageCount)
                // Render the current page with the configured DPI
                val renderedImage = renderer.renderImageWithDPI(pageIndex, config.dpi)

                // Emit the final image
                emit(renderedImage)

                // Flush the image from memory after emission
                renderedImage.flush()
                logger.debug("Successfully rendered PDF page {}/{}", pageIndex + 1, pageCount)
            }
        }

        logger.info("Completed PDF reading process successfully")

    }
}
