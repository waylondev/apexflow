package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.image.ApexImageReader
import dev.waylon.apexflow.image.ImageConstants
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
     * Whether to read pages in parallel
     */
    var parallelReading: Boolean = false

    /**
     * Maximum number of pages to read in parallel
     */
    var parallelism: Int = Runtime.getRuntime().availableProcessors()

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

    private val logger = createLogger<PdfImageReader>()

    /**
     * Read PDF pages and return a Flow of BufferedImage with streaming behavior
     *
     * Each page is rendered as a separate BufferedImage with the configured DPI
     * Supports parallel rendering while maintaining streaming characteristics
     *
     * @return Flow<BufferedImage> Flow of rendered pages in original order
     */
    override fun read(): Flow<BufferedImage> = flow {
        logger.info("Starting PDF reading process with DPI: {}, parallelism: {}", 
                   config.dpi, config.parallelism)

        // Read all bytes first to make input thread-safe for multiple PDFRenderer instances
        val pdfBytes = inputStream.readAllBytes()
        logger.debug("Read {} bytes from input stream", pdfBytes.size)

        // Create initial document to get page count
        Loader.loadPDF(RandomAccessReadBuffer(pdfBytes)).use { document ->
            val pageCount = document.numberOfPages
            logger.info("Found {} pages in PDF document", pageCount)

            val pagesToRender = if (config.pageNumbers.isEmpty()) {
                0 until pageCount
            } else {
                config.pageNumbers.filter { it in 0 until pageCount }
            }

            // Use direct flatMapMerge for parallel processing with thread-safe PDFRenderer per coroutine
            pagesToRender.asFlow()
                .flatMapMerge(config.parallelism) {
                    pageIndex ->
                        flow {
                            logger.debug("Rendering PDF page {}/{} in thread {}", 
                                        pageIndex + 1, pageCount, Thread.currentThread().name)
                            // Create a new PDFRenderer per coroutine (PDFBox renderers are not thread-safe)
                            Loader.loadPDF(RandomAccessReadBuffer(pdfBytes)).use { threadDocument ->
                                val threadRenderer = PDFRenderer(threadDocument)
                                emit(threadRenderer.renderImageWithDPI(pageIndex, config.dpi))
                            }
                        }.flowOn(Dispatchers.IO)
                }
                .collect {
                    image ->
                        emit(image)
                        image.flush()
                        logger.debug("Successfully rendered PDF page")
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