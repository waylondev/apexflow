package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.image.ApexImageWriter
import dev.waylon.apexflow.image.ImageConstants
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlinx.coroutines.flow.Flow

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
    var compressionType: String = ImageConstants.DEFAULT_TIFF_COMPRESSION_TYPE

    /**
     * Compression quality (0-100)
     * Applicable for lossy compression types like JPEG
     */
    var compressionQuality: Float = ImageConstants.DEFAULT_TIFF_COMPRESSION_QUALITY

    /**
     * Whether to write pages in parallel
     */
    var parallelWriting: Boolean = false

    /**
     * TIFF photometric interpretation
     */
    var photometricInterpretation: PhotometricInterpretation = PhotometricInterpretation.RGB

    /**
     * TIFF photometric interpretation sealed class
     * Provides type-safe extensibility for different photometric interpretations
     */
    sealed class PhotometricInterpretation {
        /** RGB color space */
        object RGB : PhotometricInterpretation()

        /** Grayscale color space */
        object GRAY : PhotometricInterpretation()

        /** Black and white (monochrome) */
        object BLACK_IS_WHITE : PhotometricInterpretation()

        /** CMYK color space */
        object CMYK : PhotometricInterpretation()

        /** Custom photometric interpretation with specified value */
        data class Custom(val value: Int) : PhotometricInterpretation()
    }
}

/**
 * TIFF writer implementation using TwelveMonkeys ImageIO library
 *
 * Supports writing to OutputStream with direct streaming behavior
 * Writes BufferedImage to TIFF files
 */
class TiffWriter @JvmOverloads constructor(
    private val outputStream: OutputStream,
    private val config: TiffWriterConfig = TiffWriterConfig()
) : ApexImageWriter {
    /**
     * Convenience constructor: File + configuration
     */
    @JvmOverloads
    constructor(
        file: File,
        config: TiffWriterConfig = TiffWriterConfig()
    ) : this(file.outputStream(), config)

    private val logger = createLogger<TiffWriter>()

    /**
     * Write BufferedImage flow to TIFF OutputStream with direct streaming behavior
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
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

                logger.trace("Writing TIFF page {}", pageIndex)

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