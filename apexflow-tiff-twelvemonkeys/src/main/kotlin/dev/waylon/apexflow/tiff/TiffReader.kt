package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.workflow.WorkflowReader
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * TIFF reader configuration DSL
 *
 * Provides a fluent API for configuring TIFF reader
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
 *
 * DSL usage example:
 * ```kotlin
 * val reader = TiffReader(inputStream) {
 *     // Optional custom read param for advanced configuration
 *     val customReadParam = ImageIO.getImageReadersByFormatName("tiff").next().defaultReadParam
 *     customReadParam.sourceRegion = Rectangle(0, 0, 100, 100)
 *     readParam = customReadParam
 * }
 * ```
 */
class TiffReader(
    private val inputStream: InputStream,
    private val config: TiffReaderConfig.() -> Unit = {}
) : WorkflowReader<BufferedImage> {
    
    // Configuration instance
    private val tiffConfig = TiffReaderConfig().apply(config)

    /**
     * Configure TIFF reader using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: TiffReaderConfig.() -> Unit) {
        tiffConfig.apply(config)
    }

    /**
     * Set custom ImageReadParam for advanced configuration
     *
     * @param readParam Custom ImageReadParam for advanced configuration
     */
    fun setReadParam(readParam: ImageReadParam) {
        tiffConfig.readParam = readParam
    }

    /**
     * Read TIFF data from InputStream and return a Flow of BufferedImage
     *
     * For multi-page image files, returns each page as a separate BufferedImage
     *
     * @return Flow<BufferedImage> Flow of images from the TIFF data
     */
    override fun read(): Flow<BufferedImage> = flow<BufferedImage> { // Explicit type declaration
        // Create ImageInputStream from the provided InputStream
        ImageIO.createImageInputStream(inputStream).use { imageInputStream ->
            // Get appropriate ImageReader
            val reader = getImageReader(imageInputStream)

            // Use extension function to automatically dispose reader resources
            reader.use { imageReader ->
                imageReader.input = imageInputStream

                val numPages = imageReader.getNumImages(true)

                // Use custom read param if provided, otherwise default
                val readParam = tiffConfig.readParam ?: imageReader.defaultReadParam

                // Read each page
                repeat(numPages) { pageIndex ->
                    val image = imageReader.read(pageIndex, readParam)
                    emit(image)
                }
            }
        }
    }.catch {
        // Centralized error handling
        throw it
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
                throw IllegalStateException(
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
