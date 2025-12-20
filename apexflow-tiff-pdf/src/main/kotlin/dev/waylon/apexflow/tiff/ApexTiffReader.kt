package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.ApexFlowReader
import dev.waylon.apexflow.core.util.createLogger
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

/**
 * ApexFlow TIFF reader component
 *
 * Flow<Unit> -> Flow<BufferedImage>
 *
 * Direct implementation of ApexFlowReader interface with complete TIFF reading logic
 */
class ApexTiffReader private constructor(
    private val input: () -> InputStream,
    private val config: TiffConfig = TiffConfig()
) : ApexFlowReader<BufferedImage> {

    companion object {
        /**
         * Create a TIFF reader from file path
         */
        fun fromPath(filePath: String, config: TiffConfig = TiffConfig()): ApexTiffReader {
            return ApexTiffReader({ File(filePath).inputStream() }, config)
        }

        /**
         * Create a TIFF reader from file
         */
        fun fromFile(file: File, config: TiffConfig = TiffConfig()): ApexTiffReader {
            return ApexTiffReader({ file.inputStream() }, config)
        }

        /**
         * Create a TIFF reader from input stream
         */
        fun fromInputStream(inputStream: InputStream, config: TiffConfig = TiffConfig()): ApexTiffReader {
            return ApexTiffReader({ inputStream }, config)
        }
    }

    private val logger = createLogger<ApexTiffReader>()

    override fun fromFile(file: File): ApexFlowReader<BufferedImage> {
        return fromFile(file, config)
    }

    override fun fromInputStream(inputStream: InputStream): ApexFlowReader<BufferedImage> {
        return fromInputStream(inputStream, config)
    }

    override fun fromPath(filePath: String): ApexFlowReader<BufferedImage> {
        return fromPath(filePath, config)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun transform(input: Flow<Unit>): Flow<BufferedImage> {
        return input.flatMapMerge {
            readImages()
        }
    }

    /**
     * Read images from TIFF as a flow
     * Uses use() for automatic resource management
     * No try-catch blocks, relying on use() for resource cleanup
     */
    private fun readImages(): Flow<BufferedImage> {
        return flow {
            logger.info("ApexFlow TIFF reading started")

            // Use use() for automatic resource management
            input().use { inputStream ->
                ImageIO.createImageInputStream(inputStream).use { imageInputStream ->
                    val readers = ImageIO.getImageReadersByFormatName("TIFF")
                    check(readers.hasNext()) { "No TIFF reader found" }

                    val reader = readers.next()
                    reader.input = imageInputStream

                    val pageCount = reader.getNumImages(true)
                    logger.info("Found $pageCount pages in TIFF")

                    for (pageIndex in 0 until pageCount) {
                        logger.info("Reading page $pageIndex")
                        val image = reader.read(pageIndex)
                        emit(image)
                    }
                }
            }
        }
    }
}