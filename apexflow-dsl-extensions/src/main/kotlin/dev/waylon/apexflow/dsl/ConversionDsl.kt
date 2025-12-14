package dev.waylon.apexflow.dsl

import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.tiff.TiffReader
import dev.waylon.apexflow.tiff.TiffWriter
import java.awt.image.BufferedImage

/**
 * Simplified TIFF to PDF conversion
 *
 * This function provides a concise way to create a TIFF to PDF conversion workflow
 * with minimal configuration.
 *
 * @param inputStream Input stream for TIFF file
 * @param outputStream Output stream for PDF file
 * @return Configured workflow engine ready to execute
 *
 * Usage example:
 * ```kotlin
 * val inputStream = FileInputStream("input.tif")
 * val outputStream = FileOutputStream("output.pdf")
 * val engine = tiffToPdf(inputStream, outputStream)
 * runBlocking { engine.startAsync() }
 * ```
 */
fun tiffToPdf(
    inputStream: java.io.InputStream,
    outputStream: java.io.OutputStream
) = apexFlowWorkflow {
    reader(TiffReader(inputStream))
    processor(WorkflowProcessor.identity())
    writer(PdfImageWriter(outputStream))
}

/**
 * Simplified PDF to TIFF conversion
 *
 * This function provides a concise way to create a PDF to TIFF conversion workflow
 * with minimal configuration.
 *
 * @param inputStream Input stream for PDF file
 * @param outputStream Output stream for TIFF file
 * @return Configured workflow engine ready to execute
 *
 * Usage example:
 * ```kotlin
 * val inputStream = FileInputStream("input.pdf")
 * val outputStream = FileOutputStream("output.tif")
 * val engine = pdfToTiff(inputStream, outputStream)
 * runBlocking { engine.startAsync() }
 * ```
 */
fun pdfToTiff(
    inputStream: java.io.InputStream,
    outputStream: java.io.OutputStream
) = apexFlowWorkflow {
    reader(PdfImageReader(inputStream))
    processor(WorkflowProcessor.identity())
    writer(TiffWriter(outputStream))
}
