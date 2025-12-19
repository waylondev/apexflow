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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

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

        // Read all bytes first from the input stream
        val inputBytes = inputStream.readAllBytes()
        logger.debug("Read {} bytes from input stream", inputBytes.size)

        // Create ImageInputStream from bytes for initial page count
        ByteArrayInputStream(inputBytes).use { initialInputStream ->
            ImageIO.createImageInputStream(initialInputStream).use { imageInputStream ->
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

                    // Use Kotlin Flow's flatMapMerge to enable parallel reading while maintaining order
                    (0 until numPages).asFlow()
                        .flatMapMerge(config.parallelism) { pageIndex ->
                            flow {
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
                                            emit(image)
                                        }
                                    }
                                }
                            }.flowOn(Dispatchers.IO)  // Execute in IO thread pool to avoid blocking
                        }
                        .collect { image ->
                            emit(image)
                            image.flush()
                            logger.debug("Successfully read TIFF page")
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
