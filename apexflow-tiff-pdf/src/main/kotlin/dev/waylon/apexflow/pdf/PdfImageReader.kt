package dev.waylon.apexflow.pdf

import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
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


}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Supports reading from InputStream with streaming behavior
 */
class PdfImageReader(
    private val inputStream: InputStream,
    private val config: PdfImageReaderConfig = PdfImageReaderConfig()
) {

    constructor(
        inputStream: InputStream,
        config: PdfImageReaderConfig.() -> Unit
    ) : this(inputStream, PdfImageReaderConfig().apply(config))

    constructor(
        file: File
    ) : this(file.inputStream())

    constructor(
        file: File,
        config: PdfImageReaderConfig.() -> Unit
    ) : this(file.inputStream(), config)

    private val logger = LoggerFactory.getLogger(PdfImageReader::class.java)

    /**
     * Read PDF pages and return a Flow of BufferedImage with streaming behavior
     *
     * Each page is rendered as a separate BufferedImage with the configured DPI
     *
     * @return Flow<BufferedImage> Flow of rendered pages
     */
    fun read(): Flow<BufferedImage> = flow {
        logger.info("Starting PDF reading process with DPI: {}", config.dpi)

        // PDFBox 3.0.1 supports ByteArray, so we'll convert InputStream to ByteArray
        val bytes = inputStream.use { it.readAllBytes() }
        Loader.loadPDF(bytes).use { document ->
            logger.debug("Loaded PDF document successfully")

            val renderer = PDFRenderer(document)
            val pageCount = document.numberOfPages
            logger.info("Found {} pages in PDF document", pageCount)

            val pagesToRender = 0 until pageCount

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

    /**
     * Configure PDF reader settings
     */
    fun configure(block: PdfImageReaderConfig.() -> Unit) {
        block(config)
        logger.debug("Configured PDF reader with DPI: {}", config.dpi)
    }
}