package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.workflow.WorkflowReader
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
        // PDF text reading implementation will be added later
        // For now, just emit dummy text
        emit("PDF text content")
    }
}
