package dev.waylon.apexflow.tiff

import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory

/**
 * TIFF writer configuration
 */
class TiffWriterConfig {
    /** Image write parameters for customizing TIFF writing behavior */
    var writeParam: ImageWriteParam? = null

    /**
     * Compression type to use
     * Common values: "JPEG", "LZW", "DEFLATE", "NONE"
     */
    var compressionType: String = "JPEG"

    /**
     * Compression quality (0-100)
     * Applicable for lossy compression types like JPEG
     */
    var compressionQuality: Float = 85f

    /**
     * Whether to write pages in parallel
     */
    var parallelWriting: Boolean = false

    /**
     * TIFF photometric interpretation
     */
    var photometricInterpretation: PhotometricInterpretation = PhotometricInterpretation.RGB

    /**
     * TIFF photometric interpretation enumeration
     */
    enum class PhotometricInterpretation {
        /** RGB color space */
        RGB,

        /** Grayscale color space */
        GRAY,

        /** Black and white (monochrome) */
        BLACK_IS_WHITE,

        /** CMYK color space */
        CMYK
    }
}

/**
 * TIFF writer implementation using TwelveMonkeys ImageIO library
 *
 * Supports writing to OutputStream with direct streaming behavior
 * Writes BufferedImage to TIFF files
 */
class TiffWriter(
    private val outputStream: OutputStream,
    private val config: TiffWriterConfig = TiffWriterConfig()
) {
    /**
     * 便捷构造函数：File + 配置对象
     */
    constructor(
        file: File,
        config: TiffWriterConfig = TiffWriterConfig()
    ) : this(file.outputStream(), config)

    private val logger = LoggerFactory.getLogger(TiffWriter::class.java)

    /**
     * Write BufferedImage flow to TIFF OutputStream with direct streaming behavior
     *
     * @param data Flow of BufferedImage to write
     */
    suspend fun write(data: Flow<BufferedImage>) {
        logger.info("Starting TIFF writing process")

        ImageIO.createImageOutputStream(outputStream).use { imageOutputStream ->
            // Get TIFF ImageWriter
            val writerIterator = ImageIO.getImageWritersByFormatName("tiff")

            val writer = writerIterator.next()
            writer.output = imageOutputStream
            logger.debug("Using TIFF writer: {}", writer::class.simpleName)

            // Create write param with JPEG compression
            val writeParam = config.writeParam ?: writer.defaultWriteParam

            // Step 1: Prepare write sequence for multi-page TIFF
            writer.prepareWriteSequence(null)
            logger.debug("Prepared write sequence for multi-page TIFF")

            var pageIndex = 0
            data.collect { image ->
                pageIndex++
                // Create IIOImage from BufferedImage
                val iioImage = IIOImage(image, null, null)

                logger.debug("Writing TIFF page {}", pageIndex)

                // Step 2: Write each image to the sequence immediately as it arrives
                // This ensures each image is added as a new page in the TIFF file
                writer.writeToSequence(
                    iioImage,
                    writeParam
                )
                // Always flush the image from memory immediately after writing
                image.flush()
                logger.debug("Successfully wrote TIFF page {}", pageIndex)
            }

            // Step 3: End the write sequence to finalize the multi-page TIFF file
            writer.endWriteSequence()
            logger.debug("Ended write sequence for multi-page TIFF")
        }

        logger.info("Completed TIFF writing process successfully")
    }
}

/**
 * Extension function: Convert OutputStream to TiffWriter with lambda configuration
 *
 * @param config Lambda function to configure TIFF writer settings
 * @return TiffWriter instance with specified configuration
 */
fun OutputStream.toTiffWriter(config: TiffWriterConfig.() -> Unit = {}): TiffWriter {
    return TiffWriter(this, TiffWriterConfig().apply(config))
}

/**
 * Extension function: Convert File to TiffWriter with lambda configuration
 *
 * @param config Lambda function to configure TIFF writer settings
 * @return TiffWriter instance with specified configuration
 */
fun File.toTiffWriter(config: TiffWriterConfig.() -> Unit = {}): TiffWriter {
    return TiffWriter(this, TiffWriterConfig().apply(config))
}