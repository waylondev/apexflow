package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.core.dsl.withPerformanceMonitoring
import dev.waylon.apexflow.core.dsl.withTiming
import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.pdf.PdfImageWriterConfig
import dev.waylon.apexflow.tiff.TiffReader
import dev.waylon.apexflow.tiff.TiffReaderConfig
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList

/**
 * TIFF to PDF conversion DSL
 *
 * Provides a fluent API for converting TIFF files to PDF format
 * with support for multiple input/output types and flexible configurations.
 *
 * Core features:
 * - Type-safe DSL with clear configuration options
 * - Support for File, InputStream, OutputStream, and String paths
 * - Configurable JPEG quality, PDF version, and compression settings
 * - Asynchronous processing using Kotlin coroutines
 * - Stream-based processing for efficient memory usage
 * - Comprehensive extension functions for easy integration
 *
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * tiffToPdf(
 *     tiffConfig = { /* TIFF reader config */ },
 *     pdfConfig = { jpegQuality = 90f; compressContent = true; pdfVersion = "1.7" }
 * ).convert(inputFile, outputFile)
 *
 * // Convert from String path to String path with default settings
 * tiffToPdf().convert("input.tiff", "output.pdf")
 *
 * // Convert from InputStream to OutputStream
 * tiffToPdf().convert(inputStream, outputStream)
 *
 * // Use extension functions for concise syntax
 * inputFile.toPdf(outputFile)
 * inputStream.toPdf(outputStream)
 *
 * // With custom configurations using extension functions
 * inputFile.toPdf(outputFile) {
 *     // TIFF config options
 * } pdfConfig {
 *     jpegQuality = 85f
 *     compressContent = true
 * }
 * ```
 */
fun tiffToPdf(
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
): TiffToPdfConverter {
    val readerConfig = TiffReaderConfig().apply(tiffConfig)
    val writerConfig = PdfImageWriterConfig().apply(pdfConfig)
    return TiffToPdfConverter(readerConfig, writerConfig)
}

/**
 * TIFF to PDF converter implementation
 */
class TiffToPdfConverter internal constructor(
    private val tiffConfig: TiffReaderConfig,
    private val pdfConfig: PdfImageWriterConfig
) {

    /**
     * Convert TIFF file to PDF file
     *
     * @param inputFile Input TIFF file
     * @param outputFile Output PDF file
     */
    suspend fun convert(inputFile: File, outputFile: File) {
        // Delegate to stream version - reduce code duplication
        inputFile.inputStream().use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                convert(inputStream, outputStream)
            }
        }
    }

    /**
     * Convert TIFF InputStream to PDF OutputStream
     *
     * @param inputStream Input TIFF InputStream
     * @param outputStream Output PDF OutputStream
     */
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        // Create a simple flow with the input stream
        val inputFlow = flow { emit(inputStream to outputStream) }

        // Stage 1: TIFF Reading - Only responsible for reading TIFF pages
        val tiffReadFlow =
            apexFlow<Pair<InputStream, OutputStream>, Pair<OutputStream, Flow<java.awt.image.BufferedImage>>> {
                transformOnIO { (input, output) ->
                    val imagesFlow = TiffReader(input, tiffConfig)
                        .read()
                        .withTiming("dev.waylon.apexflow.tiff.reader")
                        .flowOn(Dispatchers.IO)

                    Pair(output, imagesFlow)
                }
            }
                .withTiming("TIFF Reading Stage")

        // Stage 2: PDF Writing - Only responsible for writing PDF pages
        val pdfWriteFlow = apexFlow<Pair<OutputStream, Flow<java.awt.image.BufferedImage>>, Unit> {
            transformOnIO { (output, imagesFlow) ->
                PdfImageWriter(output, pdfConfig)
                    .write(imagesFlow)
            }
        }
            .withTiming("PDF Writing Stage")

        // Combine stages into complete flow
        val tiffToPdfFlow = tiffReadFlow + pdfWriteFlow
            .withTiming("Total TIFF to PDF Conversion")
            .withPerformanceMonitoring("TIFF to PDF Performance")

        // Execute the combined flow
        tiffToPdfFlow.transform(inputFlow).toList()
    }

    /**
     * Convert TIFF file to PDF file using string paths
     *
     * @param inputPath Input TIFF file path
     * @param outputPath Output PDF file path
     */
    suspend fun convert(inputPath: String, outputPath: String) {
        val inputFile = File(inputPath)
        val outputFile = File(outputPath)
        convert(inputFile, outputFile)
    }
}

/**
 * Extension function: Convert File to PDF with DSL configuration
 *
 * @param outputFile Output PDF file
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun File.toPdf(
    outputFile: File,
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, outputFile)
}

/**
 * Extension function: Convert File to PDF with output stream
 *
 * @param outputStream Output PDF stream
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun File.toPdf(
    outputStream: OutputStream,
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this.inputStream(), outputStream)
}

/**
 * Extension function: Convert File to PDF with string path
 *
 * @param outputPath Output PDF file path
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun File.toPdf(
    outputPath: String,
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, File(outputPath))
}

/**
 * Extension function: Convert InputStream to PDF with output file
 *
 * @param outputFile Output PDF file
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun InputStream.toPdf(
    outputFile: File,
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, outputFile.outputStream())
}

/**
 * Extension function: Convert InputStream to PDF with output stream
 *
 * @param outputStream Output PDF stream
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun InputStream.toPdf(
    outputStream: OutputStream,
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, outputStream)
}

/**
 * Extension function: Convert InputStream to PDF with string path
 *
 * @param outputPath Output PDF file path
 * @param tiffConfig TIFF reading configuration
 * @param pdfConfig PDF writing configuration
 */
suspend fun InputStream.toPdf(
    outputPath: String,
    tiffConfig: TiffReaderConfig.() -> Unit = {},
    pdfConfig: PdfImageWriterConfig.() -> Unit = {}
) {
    tiffToPdf(tiffConfig, pdfConfig).convert(this, File(outputPath).outputStream())
}