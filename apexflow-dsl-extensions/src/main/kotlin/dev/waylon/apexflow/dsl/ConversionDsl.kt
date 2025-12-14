package dev.waylon.apexflow.dsl

import dev.waylon.apexflow.core.workflow.WorkflowProcessor
import dev.waylon.apexflow.core.workflow.apexFlowWorkflow
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.tiff.TiffReader
import dev.waylon.apexflow.tiff.TiffWriter

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
 * Simplified TIFF to PDF conversion using file paths
 *
 * This function provides a convenient way to create a TIFF to PDF conversion workflow
 * directly from file paths, handling resource management internally.
 *
 * @param inputPath Path to input TIFF file
 * @param outputPath Path to output PDF file
 * @return Configured workflow engine ready to execute
 *
 * Usage example:
 * ```kotlin
 * val engine = tiffToPdf("input.tif", "output.pdf")
 * runBlocking { engine.startAsync() }
 * ```
 */
fun tiffToPdf(
    inputPath: String,
    outputPath: String
) = apexFlowWorkflow {
    reader(TiffReader(java.io.FileInputStream(inputPath)))
    processor(WorkflowProcessor.identity())
    writer(PdfImageWriter(java.io.FileOutputStream(outputPath)))
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

/**
 * Simplified PDF to TIFF conversion using file paths
 *
 * This function provides a convenient way to create a PDF to TIFF conversion workflow
 * directly from file paths, handling resource management internally.
 *
 * @param inputPath Path to input PDF file
 * @param outputPath Path to output TIFF file
 * @return Configured workflow engine ready to execute
 *
 * Usage example:
 * ```kotlin
 * val engine = pdfToTiff("input.pdf", "output.tif")
 * runBlocking { engine.startAsync() }
 * ```
 */
fun pdfToTiff(
    inputPath: String,
    outputPath: String
) = apexFlowWorkflow {
    reader(PdfImageReader(java.io.FileInputStream(inputPath)))
    processor(WorkflowProcessor.identity())
    writer(TiffWriter(java.io.FileOutputStream(outputPath)))
}
