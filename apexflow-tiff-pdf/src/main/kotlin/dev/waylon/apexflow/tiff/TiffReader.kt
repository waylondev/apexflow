package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.image.ApexImageReader
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * TIFF reader configuration
 */
class TiffReaderConfig {
    /** Image read parameters for customizing TIFF reading behavior */
    var readParam: ImageReadParam? = null

    /** Whether to read pages in parallel */
    var parallelReading: Boolean = false

    /** Maximum number of pages to read in parallel */
    var parallelism: Int = Runtime.getRuntime().availableProcessors()

}

/**
 * TIFF reader implementation using TwelveMonkeys ImageIO library
 *
 * Supports reading from InputStream with configurable options
 * Reads single and multi-page TIFF files
 */
class TiffReader @JvmOverloads constructor(
    private val inputStream: InputStream,
    private val config: TiffReaderConfig = TiffReaderConfig()
) : ApexImageReader {
    /**
     * Convenience constructor: File + configuration
     */
    @JvmOverloads
    constructor(
        file: File,
        config: TiffReaderConfig = TiffReaderConfig()
    ) : this(file.inputStream(), config)

    // Logger instance using unified logging utility
    private val logger = createLogger<TiffReader>()

    /**
     * Read TIFF data from InputStream and return a Flow of BufferedImage
     *
     * For multi-page image files, returns each page as a separate BufferedImage
     * Supports parallel reading when parallelReading is enabled while maintaining streaming characteristics
     *
     * @return Flow<BufferedImage> Flow of images from the TIFF data in original order
     */
    override fun read(): Flow<BufferedImage> = flow {
        logger.info(
            "Starting TIFF reading process, parallelReading: {}, parallelism: {}",
            config.parallelReading, config.parallelism
        )

        // Create ImageInputStream from the provided InputStream
        ImageIO.createImageInputStream(inputStream).use { imageInputStream ->
            // Get appropriate ImageReader
            val reader = getImageReader(imageInputStream)
            logger.debug("Using TIFF reader: {}", reader::class.simpleName)

            // Use extension function to automatically dispose reader resources
            reader.use { imageReader ->
                imageReader.input = imageInputStream

                val numPages = imageReader.getNumImages(true)
                logger.info("Found {} pages in TIFF file", numPages)

                // Use custom read param if provided, otherwise default
                val readParam = config.readParam ?: imageReader.defaultReadParam

                if (config.parallelReading && numPages > 1) {
                    // Parallel reading mode while maintaining streaming behavior
                    logger.debug("Using parallel streaming reading mode for {} pages", numPages)

                    // Create a channel to hold read images with their original indices
                    val channel = Channel<Pair<Int, BufferedImage>>(config.parallelism)

                    // Read the entire input stream into memory for thread-safe access
                    val inputBytes = inputStream.markSupported().let {
                        if (it) {
                            inputStream.reset()
                            inputStream.readAllBytes()
                        } else {
                            // Already consumed the input stream, use the bytes we have
                            ByteArray(0) // This case shouldn't happen since we already read the numPages
                        }
                    }

                    // Run in parallel using coroutines
                    coroutineScope {
                        // Start producer coroutines for parallel reading
                        val producerJobs = (0 until numPages).map { pageIndex ->
                            launch(Dispatchers.IO) { // Use IO dispatcher for blocking operations
                                logger.debug(
                                    "Reading TIFF page {}/{} in thread {}",
                                    pageIndex + 1, numPages, Thread.currentThread().name
                                )

                                // Create a new ImageReader instance for each thread (ImageReader is not thread-safe)
                                ByteArrayInputStream(inputBytes).use { threadInputStream ->
                                    ImageIO.createImageInputStream(threadInputStream).use { threadImageInputStream ->
                                        getImageReader(threadImageInputStream).use { threadReader ->
                                            threadReader.input = threadImageInputStream
                                            val threadReadParam = config.readParam ?: threadReader.defaultReadParam
                                            val image = threadReader.read(pageIndex, threadReadParam)

                                            // Send the result with its original index to maintain order
                                            channel.send(pageIndex to image)
                                        }
                                    }
                                }
                            }
                        }

                        // Start consumer coroutine to emit images in order
                        launch { // Run in the same context as the flow
                            // Track which index we're expecting next
                            var nextExpectedIndex = 0
                            // Map to store images that arrived out of order
                            val imageMap = mutableMapOf<Int, BufferedImage>()

                            // Collect all read images
                            repeat(producerJobs.size) {
                                val (originalIndex, image) = channel.receive()

                                if (originalIndex == nextExpectedIndex) {
                                    // This is the next image we need, emit it immediately
                                    emit(image)
                                    image.flush()
                                    nextExpectedIndex++

                                    // Check if any subsequent images are already available
                                    while (imageMap.containsKey(nextExpectedIndex)) {
                                        val nextImage = imageMap.remove(nextExpectedIndex)!!
                                        emit(nextImage)
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

                        // Wait for all producer jobs to complete
                        for (job in producerJobs) {
                            job.join()
                        }

                    }
                } else {
                    // Sequential reading mode (default)
                    logger.debug("Using sequential reading mode for {} pages", numPages)

                    // Read each page
                    repeat(numPages) { pageIndex ->
                        logger.debug("Reading TIFF page {}/{}", pageIndex + 1, numPages)
                        val image = imageReader.read(pageIndex, readParam)
                        emit(image)
                        image.flush()
                        logger.debug("Successfully read TIFF page {}/{}", pageIndex + 1, numPages)
                    }
                }
            }
        }

        logger.info("Completed TIFF reading process successfully")

    }

    /**
     * Get appropriate ImageReader for the given ImageInputStream
     */
    private fun getImageReader(input: ImageInputStream): ImageReader {
        val readers = ImageIO.getImageReaders(input)
        return if (readers.hasNext()) {
            readers.next()
        } else {
            val readersByFormat = ImageIO.getImageReadersByFormatName("tiff")
            readersByFormat.next()
        }
    }
}

/**
 * Extension function to automatically dispose ImageReader resources
 */
private inline fun <R> ImageReader.use(block: (ImageReader) -> R): R {
    return try {
        block(this)
    } finally {
        this.dispose()
    }
}

/**
 * Extension function: Convert InputStream to TiffReader with lambda configuration
 *
 * @param config Lambda function to configure TIFF reader settings
 * @return TiffReader instance with specified configuration
 */
fun InputStream.toTiffReader(config: TiffReaderConfig.() -> Unit = {}): TiffReader {
    return TiffReader(this, TiffReaderConfig().apply(config))
}

/**
 * Extension function: Convert File to TiffReader with lambda configuration
 *
 * @param config Lambda function to configure TIFF reader settings
 * @return TiffReader instance with specified configuration
 */
fun File.toTiffReader(config: TiffReaderConfig.() -> Unit = {}): TiffReader {
    return TiffReader(this, TiffReaderConfig().apply(config))
}