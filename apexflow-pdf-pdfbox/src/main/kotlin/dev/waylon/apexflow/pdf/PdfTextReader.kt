package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowReader
import java.io.InputStream
import kotlin.math.min
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.text.PDFTextStripper

/**
 * PDF text reader configuration DSL
 */
class PdfTextReaderConfig {
    var startPage: Int = 1
    var endPage: Int = Int.MAX_VALUE
}

/**
 * PDF text reader implementation using PDFBox library
 *
 * Only supports reading from InputStream
 */
class PdfTextReader(
    private val inputStream: InputStream,
    private val config: PdfTextReaderConfig.() -> Unit = {}
) : WorkflowReader<String> {

    private val pdfConfig = PdfTextReaderConfig().apply(config)

    fun configure(config: PdfTextReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
    }

    override fun read(): Flow<String> = flow {
        Loader.loadPDF(RandomAccessReadBuffer(inputStream)).use { document ->
            val textStripper = PDFTextStripper()
            textStripper.startPage = pdfConfig.startPage
            textStripper.endPage = min(pdfConfig.endPage, document.numberOfPages)
            val text = textStripper.getText(document)
            emit(text)
        }
    }
}
