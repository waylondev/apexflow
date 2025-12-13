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
import org.slf4j.LoggerFactory

/**
 * TIFF file writer implementation using TwelveMonkeys ImageIO library
 *
 * Implements FileWorkflowWriter interface for writing BufferedImage to TIFF files
 * Supports writing single and multi-page TIFF files
 * Optimized for high performance
 */
class TiffWriter(
    // Optional output path to set during construction
    private var outputPath: String
) : FileWorkflowWriter<BufferedImage> {

    // Lazy logger initialization for better startup performance
    private val logger by lazy { LoggerFactory.getLogger(TiffWriter::class.java) }

    /**
     * Set the output TIFF file path
     *
     * @param filePath Path to the output TIFF file
     */
    override fun setOutput(filePath: String) {
        this.outputPath = filePath
        if (logger.isDebugEnabled) {
            logger.debug("Set TIFF output file: {}", filePath)
        }
    }

    /**
     * Write BufferedImage flow to TIFF file
     *
     * @param data Flow of BufferedImage to write
     */
    override suspend fun write(data: Flow<BufferedImage>) {
        val path = outputPath

        if (logger.isInfoEnabled) {
            logger.info("Starting TIFF generation: {}", path)
        }

        // Get TIFF image writer
        val writers = ImageIO.getImageWritersByFormatName("tiff")
        if (!writers.hasNext()) {
            throw IllegalStateException("No TIFF image writer available")
        }

        // Use extension function to automatically dispose ImageWriter resources
        writers.next().use { writer ->
            // Create output stream with use block for proper resource management
            // FileImageOutputStream is optimized for file I/O and handles buffering internally
            val file = File(path)
            FileImageOutputStream(file).use { outputStream ->
                writer.output = outputStream

                // Create default write parameters with LZW compression
                val writeParams = writer.defaultWriteParam
                writeParams.compressionMode = ImageWriteParam.MODE_EXPLICIT
                writeParams.compressionType = "LZW" // Set actual compression type (LZW is good for TIFF files)

                // Correct way to write multi-page TIFF using ImageWriter's sequence API
                var pageCount = 0

                // Step 1: Prepare write sequence for multi-page TIFF
                writer.prepareWriteSequence(null)

                data.collect { image ->
                    pageCount++

                    if (logger.isDebugEnabled) {
                        logger.debug("Writing page {} to TIFF: {}x{}", pageCount, image.width, image.height)
                    }

                    // Create IIOImage from BufferedImage
                    val iioImage = IIOImage(image, null, null)

                    // Step 2: Write each image to the sequence immediately as it arrives
                    // This ensures each image is added as a new page in the TIFF file
                    writer.writeToSequence(
                        iioImage,
                        writeParams
                    )
                    // Always flush the image from memory immediately after writing
                    image.flush()
                }

                // Step 3: End the write sequence to finalize the multi-page TIFF file
                writer.endWriteSequence()

                if (pageCount > 0) {
                    if (logger.isInfoEnabled) {
                        logger.info("TIFF generation completed: {}, pages: {}", path, pageCount)
                    }
                } else {
                    logger.warn("No images to write to TIFF file: {}", path)
                }
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
