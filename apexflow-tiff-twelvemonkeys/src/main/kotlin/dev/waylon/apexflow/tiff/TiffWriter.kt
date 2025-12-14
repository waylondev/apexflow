package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.workflow.WorkflowWriter
import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

/**
 * TIFF writer configuration DSL
 *
 * Provides a fluent API for configuring TIFF writer
 */
class TiffWriterConfig {
    /** JPEG compression quality for TIFF writing */
    var jpegQuality: Float = 85f
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
            val writeParam = writer.defaultWriteParam
            writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
            writeParam.compressionType = "JPEG"
            writeParam.compressionQuality = tiffConfig.jpegQuality / 100f

            // Write images to TIFF
            data.collect {
                writer.write(null, writer.defaultWriteParam.let { param ->
                    javax.imageio.IIOImage(it, null, null)
                }, writeParam)
            }
        }
    }
}
