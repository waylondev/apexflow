package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.util.createLogger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * ApexFlow PDF writer component
 * 
 * Flow<BufferedImage> -> Flow<Unit>
 * 
 * Direct implementation of ApexFlow interface for PDF writing
 */
class ApexPdfWriter private constructor(
    private val outputStream: OutputStream? = null,
    private val filePath: String? = null,
    private val config: PdfConfig = PdfConfig()
) : ApexFlow<BufferedImage, Unit> {
    
    companion object {
        /**
         * Create a PDF writer to file path
         */
        fun toFile(filePath: String, config: PdfConfig = PdfConfig()): ApexPdfWriter {
            return ApexPdfWriter(null, filePath, config)
        }
        
        /**
         * Create a PDF writer to output stream
         */
        fun toOutputStream(outputStream: OutputStream, config: PdfConfig = PdfConfig()): ApexPdfWriter {
            return ApexPdfWriter(outputStream, null, config)
        }
    }
    
    private val logger = createLogger<ApexPdfWriter>()
    
    override fun transform(input: Flow<BufferedImage>): Flow<Unit> {
        return flow { 
            logger.info("ApexFlow PDF writing started")
            
            val document = PDDocument()
            
            try {
                val images = mutableListOf<BufferedImage>()
                
                input.collect { image ->
                    images.add(image)
                    emit(Unit)
                }
                
                images.forEachIndexed { index, image ->
                    logger.info("Writing page $index to PDF")
                    
                    val page = PDPage(config.pageSize)
                    document.addPage(page)
                    
                    PDPageContentStream(document, page).use { contentStream ->
                        val pdImage = JPEGFactory.createFromImage(document, image, config.jpegQuality)
                        contentStream.drawImage(pdImage, 0f, 0f, page.mediaBox.width, page.mediaBox.height)
                    }
                }
                
                when {
                    outputStream != null -> document.save(outputStream)
                    filePath != null -> document.save(File(filePath))
                    else -> throw IllegalArgumentException("No output destination specified")
                }
                
                logger.info("ApexFlow PDF writing completed, wrote ${images.size} pages")
            } finally {
                document.close()
            }
        }
    }
}