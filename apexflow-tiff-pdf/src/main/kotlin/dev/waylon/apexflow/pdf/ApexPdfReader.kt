package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.ApexFlowReader
import dev.waylon.apexflow.core.util.createLogger
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
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
 * Direct implementation of ApexFlowReader interface with complete PDF reading logic
 */
class ApexPdfReader private constructor(
    private val input: () -> InputStream,
    private val config: PdfConfig = PdfConfig()
) : ApexFlowReader<BufferedImage> {

    companion object {
        /**
         * Create a PDF reader from file path
         */
        fun fromPath(filePath: String, config: PdfConfig = PdfConfig()): ApexPdfReader {
            return ApexPdfReader({ File(filePath).inputStream() }, config)
        }

        /**
         * Create a PDF reader from file
         */
        fun fromFile(file: File, config: PdfConfig = PdfConfig()): ApexPdfReader {
            return ApexPdfReader({ file.inputStream() }, config)
        }

        /**
         * Create a PDF reader from input stream
         */
        fun fromInputStream(inputStream: InputStream, config: PdfConfig = PdfConfig()): ApexPdfReader {
            return ApexPdfReader({ inputStream }, config)
        }
    }

    private val logger = createLogger<ApexPdfReader>()

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
        }.buffer(config.bufferSize) // Directly use bufferSize from config
    }

    /**
     * Read images from PDF as a flow
     * Uses use() for automatic resource management
     * No try-catch blocks, relying on resource management
     */
    private fun readImages(): Flow<BufferedImage> {
        return flow {
            logger.info("ApexFlow PDF reading started with DPI: ${config.dpi}")

            // Use use() for automatic resource management
            input().use { stream ->
                Loader.loadPDF(RandomAccessReadBuffer(stream)).use { document ->
                    val renderer = PDFRenderer(document)
                    val pageCount = document.numberOfPages

                    logger.info("Found $pageCount pages in PDF")

                    for (pageIndex in 0 until pageCount) {
                        logger.debug("Rendering page $pageIndex")
                        val image = renderer.renderImageWithDPI(pageIndex, config.dpi)
                        emit(image)
                    }
                }
            }
        }
    }
}