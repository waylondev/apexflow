package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowReader
import java.awt.image.BufferedImage
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.rendering.PDFRenderer

/**
 * PDF image reader configuration DSL
 */
class PdfImageReaderConfig {
    var dpi: Float = 100f
}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Only supports reading from InputStream
 */
class PdfImageReader(
    private val inputStream: InputStream,
    private val config: PdfImageReaderConfig.() -> Unit = {}
) : WorkflowReader<BufferedImage> {

    private val pdfConfig = PdfImageReaderConfig().apply(config)

    fun configure(config: PdfImageReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    override fun read(): Flow<BufferedImage> = flow {
        Loader.loadPDF(inputStream.readAllBytes()).use { document ->

            val renderer = PDFRenderer(document)
            val pageCount = document.numberOfPages

            repeat(pageCount) { pageIndex ->
                // Render the current page with the configured DPI
                val renderedImage = renderer.renderImageWithDPI(pageIndex, pdfConfig.dpi)

                // Emit the final image
                emit(renderedImage)

                // Flush the image from memory after emission
                renderedImage.flush()
            }
        }
    }
}
