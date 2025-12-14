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
    var dpi: Float = 300f
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
        Loader.loadPDF(RandomAccessReadBuffer(inputStream)).use { document ->
            val renderer = PDFRenderer(document)
            val numPages = document.numberOfPages

            for (pageIndex in 0 until numPages) {
                val image = renderer.renderImageWithDPI(pageIndex, pdfConfig.dpi)
                emit(image)
            }
        }
    }
}
