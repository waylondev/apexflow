package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.util.createLogger
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.rendering.PDFRenderer

/**
 * ApexFlow PDF reader component
 *
 * Flow<Unit> -> Flow<BufferedImage>
 *
 * Direct implementation of ApexFlow interface for PDF reading
 */
class ApexPdfReader private constructor(
    private val inputStream: InputStream? = null,
    private val filePath: String? = null,
    private val config: PdfConfig = PdfConfig()
) : ApexFlow<Unit, BufferedImage> {

    companion object {
        /**
         * Create a PDF reader from file path
         */
        fun fromFile(filePath: String, config: PdfConfig = PdfConfig()): ApexPdfReader {
            return ApexPdfReader(null, filePath, config)
        }

        /**
         * Create a PDF reader from input stream
         */
        fun fromInputStream(inputStream: InputStream, config: PdfConfig = PdfConfig()): ApexPdfReader {
            return ApexPdfReader(inputStream, null, config)
        }
    }

    private val logger = createLogger<ApexPdfReader>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun transform(input: Flow<Unit>): Flow<BufferedImage> {
        return input.flatMapMerge {
            readImages()
        }
    }

    /**
     * Read images from PDF as a flow
     */
    private fun readImages(): Flow<BufferedImage> {
        return flow {
            logger.info("ApexFlow PDF reading started with DPI: ${config.dpi}")

            val input: InputStream = when {
                inputStream != null -> inputStream
                filePath != null -> File(filePath).inputStream()
                else -> throw IllegalArgumentException("Either inputStream or filePath must be provided")
            }

            Loader.loadPDF(RandomAccessReadBuffer(input)).use { document ->
                val renderer = PDFRenderer(document)
                val pageCount = document.numberOfPages

                logger.info("Found $pageCount pages in PDF")

                for (pageIndex in 0 until pageCount) {
                    logger.info("Rendering page $pageIndex")
                    val image = renderer.renderImageWithDPI(pageIndex, config.dpi)
                    emit(image)
                }
            }
        }
    }
}