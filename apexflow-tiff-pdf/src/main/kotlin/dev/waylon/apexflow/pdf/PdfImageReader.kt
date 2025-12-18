package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.conversion.ConversionException
import dev.waylon.apexflow.conversion.ConversionReadException
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer

/**
 * PDF image reader configuration
 */
class PdfImageReaderConfig {
    /**
     * DPI (dots per inch) for rendering PDF pages
     * Higher values result in better quality but larger file sizes
     */
    var dpi: Float = 150f
    
    /**
     * Page numbers to render (0-based index)
     * If empty, all pages will be rendered
     */
    var pageNumbers: List<Int> = emptyList()
    
    /**
     * Whether to skip blank pages during rendering
     */
    var skipBlankPages: Boolean = false
    
    /**
     * Image type to use for rendering
     */
    var imageType: ImageType = ImageType.RGB
    
    /**
     * Image type enumeration for PDF rendering
     */
    enum class ImageType {
        /** RGB color space with 8 bits per component */
        RGB,
        /** Grayscale color space with 8 bits per component */
        GRAY,
        /** Binary (black and white) color space */
        BINARY
    }
}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Only supports reading from InputStream
 */
class PdfImageReader(
    private val inputStream: InputStream,
    private val config: PdfImageReaderConfig.() -> Unit = {}
) {

    private val logger = LoggerFactory.getLogger(PdfImageReader::class.java)
    private val pdfConfig = PdfImageReaderConfig().apply(config)

    fun configure(config: PdfImageReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
        logger.debug("Configured PDF reader with DPI: {}", pdfConfig.dpi)
    }

    /**
     * Read PDF pages from InputStream and return a Flow of BufferedImage
     *
     * Each page is rendered as a separate BufferedImage with the configured DPI
     *
     * @return Flow<BufferedImage> Flow of rendered pages
     * @throws ConversionReadException if there's an error reading the PDF data
     */
    fun read(): Flow<BufferedImage> = flow {
        try {
            logger.info("Starting PDF reading process with DPI: {}", pdfConfig.dpi)
            
            // Use inputStream directly without reading all bytes to memory
            Loader.loadPDF(inputStream).use { document ->
                logger.debug("Loaded PDF document successfully")

                val renderer = PDFRenderer(document)
                val pageCount = document.numberOfPages
                logger.info("Found {} pages in PDF document", pageCount)

                repeat(pageCount) { pageIndex ->
                    logger.debug("Rendering PDF page {}/{}", pageIndex + 1, pageCount)
                    // Render the current page with the configured DPI
                    val renderedImage = renderer.renderImageWithDPI(pageIndex, pdfConfig.dpi)

                    // Emit the final image
                    emit(renderedImage)

                    // Flush the image from memory after emission
                    renderedImage.flush()
                    logger.debug("Successfully rendered PDF page {}/{}", pageIndex + 1, pageCount)
                }
            }
            
            logger.info("Completed PDF reading process successfully")
        } catch (e: Exception) {
            logger.error("Failed to read PDF data: {}", e.message, e)
            when (e) {
                is ConversionException -> throw e
                else -> throw ConversionReadException("Failed to read PDF data", e)
            }
        }
    }
}
