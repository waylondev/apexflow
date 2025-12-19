package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.util.logger
import dev.waylon.apexflow.image.ApexImageReader
import dev.waylon.apexflow.image.ImageConstants
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.rendering.PDFRenderer

/**
 * PDF image reader configuration
 */
class PdfImageReaderConfig {
    /**
     * DPI (dots per inch) for rendering PDF pages
     * Higher values result in better quality but larger file sizes
     */
    var dpi: Float = ImageConstants.DEFAULT_DPI

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
     * Image type sealed class for PDF rendering
     * Provides type-safe extensibility for different image types
     */
    sealed class ImageType {
        /** RGB color space with 8 bits per component */
        object RGB : ImageType()

        /** Grayscale color space with 8 bits per component */
        object GRAY : ImageType()

        /** Binary (black and white) color space */
        object BINARY : ImageType()

        /** Custom image type with specified bits per component */
        data class Custom(val bitsPerComponent: Int) : ImageType()
    }
}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Supports reading from InputStream with streaming behavior
 */
class PdfImageReader @JvmOverloads constructor(
    private val inputStream: InputStream,
    private val config: PdfImageReaderConfig = PdfImageReaderConfig()
) : ApexImageReader {
    /**
     * Convenience constructor: File + configuration
     */
    @JvmOverloads
    constructor(
        file: File,
        config: PdfImageReaderConfig = PdfImageReaderConfig()
    ) : this(file.inputStream(), config)

    private val logger = logger

    /**
     * Read PDF pages and return a Flow of BufferedImage with streaming behavior
     *
     * Each page is rendered as a separate BufferedImage with the configured DPI
     *
     * @return Flow<BufferedImage> Flow of rendered pages
     */
    override fun read(): Flow<BufferedImage> = flow {
        logger.info("Starting PDF reading process with DPI: {}", config.dpi)

        // PDFBox 3.0.1 supports ByteArray, so we'll convert InputStream to ByteArray
        Loader.loadPDF(RandomAccessReadBuffer(inputStream)).use { document ->
            logger.debug("Loaded PDF document successfully")

            val renderer = PDFRenderer(document)
            val pageCount = document.numberOfPages
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

/**
 * Extension function: Convert InputStream to PdfImageReader with lambda configuration
 *
 * @param config Lambda function to configure PDF reader settings
 * @return PdfImageReader instance with specified configuration
 */
fun InputStream.toPdfImageReader(config: PdfImageReaderConfig.() -> Unit = {}): PdfImageReader {
    return PdfImageReader(this, PdfImageReaderConfig().apply(config))
}

/**
 * Extension function: Convert File to PdfImageReader with lambda configuration
 *
 * @param config Lambda function to configure PDF reader settings
 * @return PdfImageReader instance with specified configuration
 */
fun File.toPdfImageReader(config: PdfImageReaderConfig.() -> Unit = {}): PdfImageReader {
    return PdfImageReader(this, PdfImageReaderConfig().apply(config))
}