package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.workflow.FileWorkflowWriter
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageOutputStream
import kotlinx.coroutines.flow.Flow



/**
 * TIFF file writer implementation using TwelveMonkeys ImageIO library
 *
 * Implements FileWorkflowWriter interface for writing BufferedImage to TIFF files
 * Supports writing single and multi-page TIFF files with advanced performance optimizations:
 * - Direct support for ImageWriteParam for complete configuration control
 * - Optimized memory management with image flushing
 * - High performance I/O with FileImageOutputStream
 *
 * Example usage:
 * ```kotlin
 * // Basic usage with default parameters
 * val writer1 = TiffWriter(outputPath)
 * 
 * // Usage with custom ImageWriteParam for advanced configuration
 * val writer2 = TiffWriter(outputPath)
 * val customWriteParam = ImageIO.getImageWritersByFormatName("tiff").next().defaultWriteParam
 * customWriteParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
 * customWriteParam.compressionType = "DEFLATE"
 * customWriteParam.compressionQuality = 0.95f
 * writer2.setWriteParam(customWriteParam)
 * ```
 */
class TiffWriter(
    // Optional output path to set during construction
    private var outputPath: String,
    // Custom ImageWriteParam for advanced configuration, null means use default
    private var writeParam: ImageWriteParam? = null
) : FileWorkflowWriter<BufferedImage> {

    /**
     * Set the output TIFF file path
     *
     * @param filePath Path to the output TIFF file
     */
    override fun setOutput(filePath: String) {
        this.outputPath = filePath
    }
    
    /**
     * Set custom ImageWriteParam for advanced configuration
     *
     * @param writeParam Custom ImageWriteParam for advanced configuration
     */
    fun setWriteParam(writeParam: ImageWriteParam) {
        this.writeParam = writeParam
    }

    /**
     * Write BufferedImage flow to TIFF file
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
        val path = outputPath

        // Get TIFF image writer
        val writers = ImageIO.getImageWritersByFormatName("tiff")
        if (!writers.hasNext()) {
            throw IllegalStateException("No TIFF image writer available")
        }

        // Use extension function to automatically dispose ImageWriter resources
        writers.next().use { writer ->
            // Create output stream with use block for proper resource management
            val file = File(path)
            FileImageOutputStream(file).use { outputStream ->
                writer.output = outputStream

                // Use custom write param if provided, otherwise use default
                val effectiveWriteParam = writeParam ?: writer.defaultWriteParam

                // Correct way to write multi-page TIFF using ImageWriter's sequence API
                var pageCount = 0

                // Step 1: Prepare write sequence for multi-page TIFF
                writer.prepareWriteSequence(null)

                data.collect { image ->
                    pageCount++

                    // Create IIOImage from BufferedImage
                    val iioImage = IIOImage(image, null, null)

                    // Step 2: Write each image to the sequence immediately as it arrives
                    // This ensures each image is added as a new page in the TIFF file
                    writer.writeToSequence(
                        iioImage,
                        effectiveWriteParam
                    )
                    // Always flush the image from memory immediately after writing
                    image.flush()
                }

                // Step 3: End the write sequence to finalize the multi-page TIFF file
                writer.endWriteSequence()
            }
        }
    }

    /**
     * Extension function to automatically dispose ImageWriter resources
     */
    private inline fun <R> ImageWriter.use(block: (ImageWriter) -> R): R {
        return try {
            block(this)
        } finally {
            this.dispose()
        }
    }
}
