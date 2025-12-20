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
        logger.info("Starting TIFF reading process")

        // Directly use InputStream without reading all bytes into memory
        // This prevents blocking and reduces memory usage for large files
        ImageIO.createImageInputStream(inputStream).use { imageInputStream ->
            getImageReader(imageInputStream).use { reader ->
                reader.input = imageInputStream
                val numPages = reader.getNumImages(true)
                logger.info("Found {} pages in TIFF file", numPages)

                val readParam = config.readParam ?: reader.defaultReadParam

                // Sequential reading for clean, simple implementation
                for (pageIndex in 0 until numPages) {
                    logger.debug("Reading TIFF page {}/{}", pageIndex + 1, numPages)
                    val image = reader.read(pageIndex, readParam)
                    emit(image)
                    image.flush()
                    logger.debug("Successfully read TIFF page")
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
