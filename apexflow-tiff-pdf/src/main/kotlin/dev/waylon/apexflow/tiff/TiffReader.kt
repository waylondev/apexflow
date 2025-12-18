package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.conversion.ConversionException
import dev.waylon.apexflow.conversion.ConversionFormatException
import dev.waylon.apexflow.conversion.ConversionReadException
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory

/**
 * TIFF reader configuration
 */
class TiffReaderConfig {
    /** Image read parameters for customizing TIFF reading behavior */
    var readParam: ImageReadParam? = null

    /**
     * Page numbers to read (0-based index)
     * If empty, all pages will be read
     */
    var pageNumbers: List<Int> = emptyList()

    /**
     * Whether to skip blank pages during reading
     */
    var skipBlankPages: Boolean = false

    /**
     * Whether to read images in parallel
     */
    var parallelReading: Boolean = false

    /**
     * Maximum width for scaled images
     * If 0, no scaling is applied
     */
    var maxWidth: Int = 0

    /**
     * Maximum height for scaled images
     * If 0, no scaling is applied
     */
    var maxHeight: Int = 0
}

/**
 * TIFF reader implementation using TwelveMonkeys ImageIO library
 *
 * Supports reading from InputStream with configurable options
 * Reads single and multi-page TIFF files
 */
class TiffReader(
    private val inputStream: InputStream,
    private val config: TiffReaderConfig.() -> Unit = {}
) {

    // Logger instance
    private val logger = LoggerFactory.getLogger(TiffReader::class.java)

    // Configuration instance
    private val tiffConfig = TiffReaderConfig().apply(config)

    /**
     * Read TIFF data from InputStream and return a Flow of BufferedImage
     *
     * For multi-page image files, returns each page as a separate BufferedImage
     *
     * @return Flow<BufferedImage> Flow of images from the TIFF data
     * @throws ConversionReadException if there's an error reading the TIFF data
     */
    fun read(): Flow<BufferedImage> = flow {
        try {
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
                    val readParam = tiffConfig.readParam ?: imageReader.defaultReadParam

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
        } catch (e: Exception) {
            logger.error("Failed to read TIFF data: {}", e.message, e)
            when (e) {
                is ConversionException -> throw e
                else -> throw ConversionReadException("Failed to read TIFF data", e)
            }
        }
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
            if (readersByFormat.hasNext()) {
                readersByFormat.next()
            } else {
                throw ConversionFormatException(
                    "No ImageReader available for TIFF format. " +
                            "Make sure appropriate image IO plugins (like TwelveMonkeys TIFF plugin) are installed."
                )
            }
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
