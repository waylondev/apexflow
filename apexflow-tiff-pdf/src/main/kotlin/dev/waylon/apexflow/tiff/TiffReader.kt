package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.image.ApexImageReader
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * TIFF reader configuration
 */
class TiffReaderConfig {
    /** Image read parameters for customizing TIFF reading behavior */
    var readParam: ImageReadParam? = null

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
     *
     * @return Flow<BufferedImage> Flow of images from the TIFF data
     */
    override fun read(): Flow<BufferedImage> = flow {
        logger.info("Starting TIFF reading process")

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