package dev.waylon.apexflow.tiff

import java.awt.image.BufferedImage
import java.io.File
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

}

/**
 * TIFF reader implementation using TwelveMonkeys ImageIO library
 *
 * Supports reading from InputStream with configurable options
 * Reads single and multi-page TIFF files
 */
class TiffReader(
    private val inputStream: InputStream,
    private val config: TiffReaderConfig = TiffReaderConfig()
) {
    /**
     * 便捷构造函数：InputStream + 配置lambda
     */
    constructor(
        inputStream: InputStream,
        config: TiffReaderConfig.() -> Unit
    ) : this(inputStream, TiffReaderConfig().apply(config))
    
    /**
     * 便捷构造函数：File + 配置对象
     */
    constructor(
        file: File,
        config: TiffReaderConfig = TiffReaderConfig()
    ) : this(file.inputStream(), config)
    
    /**
     * 便捷构造函数：File + 配置lambda
     */
    constructor(
        file: File,
        config: TiffReaderConfig.() -> Unit
    ) : this(file.inputStream(), TiffReaderConfig().apply(config))

    // Logger instance
    private val logger = LoggerFactory.getLogger(TiffReader::class.java)

    /**
     * Read TIFF data from InputStream and return a Flow of BufferedImage
     *
     * For multi-page image files, returns each page as a separate BufferedImage
     *
     * @return Flow<BufferedImage> Flow of images from the TIFF data
     */
    fun read(): Flow<BufferedImage> = flow {
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