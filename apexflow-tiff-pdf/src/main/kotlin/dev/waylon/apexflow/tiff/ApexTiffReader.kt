package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.util.createLogger
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

/**
 * ApexFlow TIFF reader component
 * 
 * Flow<Unit> -> Flow<BufferedImage>
 * 
 * Direct implementation of ApexFlow interface for TIFF reading
 */
class ApexTiffReader private constructor(
    private val inputStream: InputStream? = null,
    private val filePath: String? = null,
    private val config: TiffConfig = TiffConfig()
) : ApexFlow<Unit, BufferedImage> {
    
    companion object {
        /**
         * Create a TIFF reader from file path
         */
        fun fromFile(filePath: String, config: TiffConfig = TiffConfig()): ApexTiffReader {
            return ApexTiffReader(null, filePath, config)
        }
        
        /**
         * Create a TIFF reader from input stream
         */
        fun fromInputStream(inputStream: InputStream, config: TiffConfig = TiffConfig()): ApexTiffReader {
            return ApexTiffReader(inputStream, null, config)
        }
    }
    
    private val logger = createLogger<ApexTiffReader>()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun transform(input: Flow<Unit>): Flow<BufferedImage> {
        return input.flatMapMerge { 
            readImages()
        }
    }
    
    /**
     * Read images from TIFF as a flow
     */
    private fun readImages(): Flow<BufferedImage> {
        return flow { 
            logger.info("ApexFlow TIFF reading started")
            
            val inputStream: ImageInputStream = when {
                this@ApexTiffReader.inputStream != null -> ImageIO.createImageInputStream(this@ApexTiffReader.inputStream)
                filePath != null -> ImageIO.createImageInputStream(File(filePath))
                else -> throw IllegalArgumentException("No input source specified")
            }
            
            try {
                val readers = ImageIO.getImageReadersByFormatName("TIFF")
                if (!readers.hasNext()) {
                    throw IllegalArgumentException("No TIFF reader found")
                }
                
                val reader = readers.next()
                reader.setInput(inputStream, true)
                
                val pageCount = reader.getNumImages(true)
                logger.info("Found $pageCount pages in TIFF")
                
                for (pageIndex in 0 until pageCount) {
                    logger.info("Reading page $pageIndex")
                    val image = reader.read(pageIndex)
                    emit(image)
                }
                
                logger.info("ApexFlow TIFF reading completed")
            } finally {
                inputStream.close()
            }
        }
    }
}