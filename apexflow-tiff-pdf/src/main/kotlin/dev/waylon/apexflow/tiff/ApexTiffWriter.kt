package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.util.createLogger
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * ApexFlow TIFF writer component
 * 
 * Flow<BufferedImage> -> Flow<Unit>
 * 
 * Direct implementation of ApexFlow interface for TIFF writing
 */
class ApexTiffWriter private constructor(
    private val outputStream: OutputStream? = null,
    private val filePath: String? = null,
    private val config: TiffConfig = TiffConfig()
) : ApexFlow<BufferedImage, Unit> {
    
    companion object {
        /**
         * Create a TIFF writer to file path
         */
        fun toFile(filePath: String, config: TiffConfig = TiffConfig()): ApexTiffWriter {
            return ApexTiffWriter(null, filePath, config)
        }
        
        /**
         * Create a TIFF writer to output stream
         */
        fun toOutputStream(outputStream: OutputStream, config: TiffConfig = TiffConfig()): ApexTiffWriter {
            return ApexTiffWriter(outputStream, null, config)
        }
    }
    
    private val logger = createLogger<ApexTiffWriter>()
    
    override fun transform(input: Flow<BufferedImage>): Flow<Unit> {
        return flow { 
            logger.info("ApexFlow TIFF writing started with compression: ${config.compressionType}")
            
            val imageOutputStream: ImageOutputStream = when {
                this@ApexTiffWriter.outputStream != null -> ImageIO.createImageOutputStream(this@ApexTiffWriter.outputStream)
                filePath != null -> ImageIO.createImageOutputStream(File(filePath))
                else -> throw IllegalArgumentException("No output destination specified")
            }
            
            try {
                val writers = ImageIO.getImageWritersByFormatName("TIFF")
                if (!writers.hasNext()) {
                    throw IllegalArgumentException("No TIFF writer found")
                }
                
                val writer = writers.next()
                writer.output = imageOutputStream
                
                val writeParam = config.writeParam ?: writer.defaultWriteParam
                writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
                writeParam.compressionType = config.compressionType
                writeParam.compressionQuality = config.compressionQuality / 100f
                
                val images = mutableListOf<BufferedImage>()
                
                input.collect { image ->
                    images.add(image)
                    emit(Unit)
                }
                
                if (images.isNotEmpty()) {
                    writer.write(null, IIOImage(images[0], null, null), writeParam)
                    
                    for (i in 1 until images.size) {
                        writer.writeInsert(i, IIOImage(images[i], null, null), writeParam)
                    }
                    
                    logger.info("ApexFlow TIFF writing completed, wrote ${images.size} pages")
                }
            } finally {
                imageOutputStream.close()
            }
        }
    }
}