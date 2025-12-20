package dev.waylon.apexflow.tiff

import dev.waylon.apexflow.core.ApexFlowWriter
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
 * Direct implementation of ApexFlowWriter interface with complete TIFF writing logic
 * Optimized to write pages as images are received, using writeInsert for all pages
 */
class ApexTiffWriter private constructor(
    private val outputProvider: () -> OutputStream,
    private val config: TiffConfig = TiffConfig()
) : ApexFlowWriter<BufferedImage> {
    
    companion object {
        /**
         * Create a TIFF writer to file path
         */
        fun toPath(filePath: String, config: TiffConfig = TiffConfig()): ApexTiffWriter {
            return ApexTiffWriter({ File(filePath).outputStream() }, config)
        }
        
        /**
         * Create a TIFF writer to file
         */
        fun toFile(file: File, config: TiffConfig = TiffConfig()): ApexTiffWriter {
            return ApexTiffWriter({ file.outputStream() }, config)
        }
        
        /**
         * Create a TIFF writer to output stream
         */
        fun toOutputStream(outputStream: OutputStream, config: TiffConfig = TiffConfig()): ApexTiffWriter {
            return ApexTiffWriter({ outputStream }, config)
        }
    }
    
    private val logger = createLogger<ApexTiffWriter>()
    
    override fun toFile(file: File): ApexFlowWriter<BufferedImage> {
        return toFile(file, config)
    }
    
    override fun toOutputStream(outputStream: OutputStream): ApexFlowWriter<BufferedImage> {
        return toOutputStream(outputStream, config)
    }
    
    override fun toPath(filePath: String): ApexFlowWriter<BufferedImage> {
        return toPath(filePath, config)
    }
    
    override fun transform(input: Flow<BufferedImage>): Flow<Unit> {
        return flow { 
            logger.info("ApexFlow TIFF writing started with compression: ${config.compressionType}")
            
            // Use use() for automatic resource management
            outputProvider().use { outputStream ->
                ImageIO.createImageOutputStream(outputStream).use { imageOutputStream ->
                    val writers = ImageIO.getImageWritersByFormatName("TIFF")
                    check(writers.hasNext()) { "No TIFF writer found" }
                    
                    val writer = writers.next()
                    writer.output = imageOutputStream
                    
                    // Use client-provided writeParam if available, otherwise create default
                    val writeParam = config.writeParam ?: writer.defaultWriteParam
                    
                    var pageIndex = 0
                    
                    // Write all pages using writeInsert - simpler and more consistent
                    input.collect { image ->
                        logger.info("Writing page $pageIndex to TIFF")
                        
                        // Use writeInsert for all pages, including the first one
                        // For an empty stream, writeInsert(0, ...) is equivalent to write(...)
                        writer.writeInsert(pageIndex, IIOImage(image, null, null), writeParam)
                        
                        pageIndex++
                        emit(Unit) // Signal completion for this page
                    }
                    
                    logger.info("ApexFlow TIFF writing completed, wrote $pageIndex pages")
                }
            }
        }
    }
}