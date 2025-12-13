package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.workflow.FileWorkflowReader
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * TIFF file reader implementation using TwelveMonkeys ImageIO library
 *
 * Implements FileWorkflowReader interface for reading TIFF files
 * Supports reading single and multi-page TIFF files
 * Optimized for high performance
 */
class TiffReader(
    // Default IO buffer size: 4 * 8192 = 32768 bytes
    private var ioBufferSize: Int = 4 * 8192,
    // Optional input path to set during construction
    private var inputPath: String
) : FileWorkflowReader<BufferedImage> {

    /**
     * Set the input TIFF file path
     *
     * @param filePath Path to the TIFF file
     */
    override fun setInput(filePath: String) {
        this.inputPath = filePath
    }

    /**
     * Set IO buffer size for reading files
     *
     * @param bufferSize Buffer size in bytes
     */
    fun setIoBufferSize(bufferSize: Int) {
        this.ioBufferSize = bufferSize
    }

    /**
     * Read TIFF file and return a Flow of BufferedImage
     *
     * For multi-page image files, returns each page as a separate BufferedImage
     * Optimized for high performance: minimal object creation, efficient IO, optimized image reading
     *
     * @return Flow<BufferedImage> Flow of images from the image file
     */
    override fun read(): Flow<BufferedImage> = flow<BufferedImage> { // Explicit type declaration
        // Step 1: Validate input path and get File object
        val file = validateInput()

        // Step 2: Create optimized ImageInputStream with buffered IO
        createOptimizedImageInputStream(file).use { input ->
            // Step 3: Get appropriate ImageReader using the same input stream
            val reader = getImageReader(input, file)

            // Step 4: Use extension function to automatically dispose reader resources
            reader.use { imageReader ->
                imageReader.input = input

                val numPages = imageReader.getNumImages(true)

                // Create optimized read param once
                val readParam = imageReader.defaultReadParam

                // Read each page with optimized parameters
                repeat(numPages) { pageIndex ->
                    // Optimized image reading
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
     * Validate input path and return File object
     *
     * @return File Validated file object
     */
    private fun validateInput(): File {
        // Step 1: Validate input path is set
        val path = inputPath

        // Step 2: Validate file exists and is a file
        return File(path).also {
            require(it.exists()) { "Image file does not exist: $path" }
            require(it.isFile) { "Path is not a file: $path" }
        }
    }

    /**
     * Create optimized ImageInputStream with buffered IO
     *
     * @param file File to create input stream for
     * @return ImageInputStream Optimized image input stream
     */
    private fun createOptimizedImageInputStream(file: File): ImageInputStream {
        // Use BufferedInputStream with configurable buffer size for better IO performance
        val bufferedInputStream = BufferedInputStream(FileInputStream(file), ioBufferSize)
        return ImageIO.createImageInputStream(bufferedInputStream)
    }

    /**
     * Get appropriate ImageReader for the given input stream and file
     *
     * @param input ImageInputStream to read from
     * @param file Image file for error information
     * @return ImageReader Appropriate reader instance
     */
    private fun getImageReader(input: ImageInputStream, file: File): ImageReader {
        val readers = ImageIO.getImageReaders(input)
        return if (readers.hasNext()) {
            readers.next()
        } else {
            val fileExtension = file.extension.lowercase()
            throw IllegalStateException(
                "No ImageReader available for file: ${file.name} (extension: $fileExtension). " +
                        "Make sure appropriate image IO plugins (like TwelveMonkeys TIFF plugin) are installed."
            )
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