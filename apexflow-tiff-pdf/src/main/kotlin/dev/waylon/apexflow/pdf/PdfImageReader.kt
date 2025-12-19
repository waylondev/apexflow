package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.image.ApexImageReader
import dev.waylon.apexflow.image.ImageConstants
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
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
     * Only applicable if parallelReading is true
     * Default: Number of CPU cores
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
     * Supports parallel rendering when parallelReading is enabled while maintaining streaming characteristics
     *
     * @return Flow<BufferedImage> Flow of rendered pages in original order
     */
    override fun read(): Flow<BufferedImage> = flow {
        logger.info(
            "Starting PDF reading process with DPI: {}, parallelReading: {}, parallelism: {}",
            config.dpi, config.parallelReading, config.parallelism
        )

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

            val pagesToRenderList = pagesToRender.toList()

            if (config.parallelReading && pagesToRenderList.size > 1) {
                // Parallel rendering mode while maintaining streaming behavior
                logger.debug("Using parallel streaming rendering mode for {} pages", pagesToRenderList.size)

                // Create a channel to hold rendered images with their original indices
                val channel = Channel<Pair<Int, BufferedImage>>(config.parallelism)

                // Run in parallel using coroutines
                coroutineScope {
                    // Start producer coroutines for parallel rendering
                    val producerJobs = startRenderingProducers(pagesToRenderList, renderer, channel, pageCount)

                    // Start consumer coroutine to emit images in order
                    startOrderingConsumer(producerJobs.size, channel, this@flow)

                    // Wait for all producer jobs to complete
                    waitForProducers(producerJobs)
                }
            } else {
                // Sequential rendering mode (default)
                logger.debug("Using sequential rendering mode for {} pages", pagesToRenderList.size)
                renderPagesSequentially(pagesToRender, renderer, this)
            }
        }

        logger.info("Completed PDF reading process successfully")
    }

    /**
     * Start producer coroutines for parallel rendering
     *
     * @param pagesToRender List of pages to render
     * @param renderer PDFRenderer instance
     * @param channel Channel to send rendered images
     * @param pageCount Total number of pages in the document
     * @return List of producer jobs
     */
    private fun CoroutineScope.startRenderingProducers(
        pagesToRender: List<Int>,
        renderer: PDFRenderer,
        channel: Channel<Pair<Int, BufferedImage>>,
        pageCount: Int
    ): List<kotlinx.coroutines.Job> {
        return pagesToRender.withIndex().map { (originalIndex, pageIndex) ->
            launch(Dispatchers.IO) { // Use IO dispatcher for blocking operations
                logger.debug(
                    "Rendering PDF page {}/{} in thread {}",
                    pageIndex + 1, pageCount, Thread.currentThread().name
                )

                // Render the page
                val renderedImage = renderer.renderImageWithDPI(pageIndex, config.dpi)

                // Send the result with its original index to maintain order
                channel.send(originalIndex to renderedImage)
            }
        }
    }

    /**
     * Start consumer coroutine to emit images in original order
     *
     * @param totalImages Total number of images to expect
     * @param channel Channel to receive rendered images
     * @param flowCollector Flow collector to emit images
     */
    private fun CoroutineScope.startOrderingConsumer(
        totalImages: Int,
        channel: Channel<Pair<Int, BufferedImage>>,
        flowCollector: FlowCollector<BufferedImage>
    ) {
        launch { // Run in the same context as the flow
            // Track which index we're expecting next
            var nextExpectedIndex = 0
            // Map to store images that arrived out of order
            val imageMap = mutableMapOf<Int, BufferedImage>()

            // Collect all rendered images
            repeat(totalImages) {
                val (originalIndex, image) = channel.receive()

                if (originalIndex == nextExpectedIndex) {
                    // This is the next image we need, emit it immediately
                    flowCollector.emit(image)
                    image.flush()
                    nextExpectedIndex++

                    // Check if any subsequent images are already available
                    while (imageMap.containsKey(nextExpectedIndex)) {
                        val nextImage = imageMap.remove(nextExpectedIndex)!!
                        flowCollector.emit(nextImage)
                        nextImage.flush()
                        nextExpectedIndex++
                    }
                } else {
                    // Store the image for later emission when its turn comes
                    imageMap[originalIndex] = image
                }
            }

            // Close the channel
            channel.close()
        }
    }

    /**
     * Wait for all producer jobs to complete
     *
     * @param producerJobs List of producer jobs
     */
    private suspend fun waitForProducers(producerJobs: List<kotlinx.coroutines.Job>) {
        for (job in producerJobs) {
            job.join()
        }
    }

    /**
     * Render PDF pages sequentially
     *
     * @param pagesToRender Pages to render
     * @param renderer PDFRenderer instance
     * @param flowCollector Flow collector to emit images
     */
    private suspend fun renderPagesSequentially(
        pagesToRender: Iterable<Int>,
        renderer: PDFRenderer,
        flowCollector: FlowCollector<BufferedImage>
    ) {
        pagesToRender.forEach { pageIndex ->
            // Render the current page with the configured DPI
            val renderedImage = renderer.renderImageWithDPI(pageIndex, config.dpi)

            // Emit the final image immediately
            flowCollector.emit(renderedImage)

            // Flush the image from memory after emission
            renderedImage.flush()
            logger.debug("Successfully rendered PDF page {}", pageIndex + 1)
        }
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