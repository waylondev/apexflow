package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.workflow.WorkflowWriter
import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlinx.coroutines.flow.Flow

/**
 * TIFF writer configuration DSL
 *
 * Provides a fluent API for configuring TIFF writer
 */
class TiffWriterConfig {
    /** JPEG compression quality for TIFF writing */
    var writeParam: ImageWriteParam? = null
}

/**
 * TIFF writer implementation using TwelveMonkeys ImageIO library
 *
 * Supports writing to OutputStream with configurable options
 * Writes BufferedImage to TIFF files
 *
 * DSL usage example:
 * ```kotlin
 * val writer = TiffWriter(outputStream) {
 *     jpegQuality = 90f
 * }
 * ```
 */
class TiffWriter(
    private val outputStream: OutputStream,
    private val config: TiffWriterConfig.() -> Unit = {}
) : WorkflowWriter<BufferedImage> {

    // Configuration instance
    private val tiffConfig = TiffWriterConfig().apply(config)

    /**
     * Configure TIFF writer using DSL
     *
     * @param config Configuration block
     */
    fun configure(config: TiffWriterConfig.() -> Unit) {
        tiffConfig.apply(config)
    }

    /**
     * Write BufferedImage flow to TIFF OutputStream
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
        // Create ImageOutputStream from the provided OutputStream
        ImageIO.createImageOutputStream(outputStream).use { imageOutputStream ->
            // Get TIFF ImageWriter
            val writer = ImageIO.getImageWritersByFormatName("tiff").next()
            writer.output = imageOutputStream

            // Create write param with JPEG compression
            val writeParam = tiffConfig.writeParam ?: writer.defaultWriteParam

            // Step 1: Prepare write sequence for multi-page TIFF
            writer.prepareWriteSequence(null)

            data.collect { image ->

                // Create IIOImage from BufferedImage
                val iioImage = IIOImage(image, null, null)

                // Step 2: Write each image to the sequence immediately as it arrives
                // This ensures each image is added as a new page in the TIFF file
                writer.writeToSequence(
                    iioImage,
                    writeParam
                )
                // Always flush the image from memory immediately after writing
                image.flush()
            }

            // Step 3: End the write sequence to finalize the multi-page TIFF file
            writer.endWriteSequence()
        }
    }
}
