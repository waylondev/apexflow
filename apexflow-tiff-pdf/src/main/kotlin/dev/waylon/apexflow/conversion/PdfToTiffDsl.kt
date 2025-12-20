package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageReaderConfig
import dev.waylon.apexflow.tiff.TiffWriter
import dev.waylon.apexflow.tiff.TiffWriterConfig
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * PDF to TIFF conversion DSL
 *
 * Provides a fluent API for converting PDF files to TIFF format
 * with support for multiple input/output types and flexible configurations.
 *
 * Core features:
 * - Type-safe DSL with clear configuration options
 * - Support for File, InputStream, OutputStream, and String paths
 * - Configurable DPI, compression, and other conversion settings
 * - Asynchronous processing using Kotlin coroutines
 * - Stream-based processing for efficient memory usage
 * - Comprehensive extension functions for easy integration
 *
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * pdfToTiff(
 *     pdfConfig = { dpi = 150f; skipBlankPages = true },
 *     tiffConfig = { compressionType = "JPEG"; compressionQuality = 90f }
 * ).convert(inputFile, outputFile)
 *
 * // Convert from String path to String path with default settings
 * pdfToTiff().convert("input.pdf", "output.tiff")
 *
 * // Convert from InputStream to OutputStream
 * pdfToTiff().convert(inputStream, outputStream)
 *
 * // Use extension functions for concise syntax
 * inputFile.toTiff(outputFile)
 * inputStream.toTiff(outputStream)
 *
 * // With custom configurations using extension functions
 * inputFile.toTiff(outputFile) {
 *     dpi = 300f
 * } tiffConfig {
 *     compressionType = "LZW"
 * }
 * ```
 */
fun pdfToTiff(
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
): PdfToTiffConverter {
    val readerConfig = PdfImageReaderConfig().apply(pdfConfig)
    val writerConfig = TiffWriterConfig().apply(tiffConfig)
    return PdfToTiffConverter(readerConfig, writerConfig)
}

/**
 * PDF to TIFF converter implementation
 */
class PdfToTiffConverter internal constructor(
    private val pdfConfig: PdfImageReaderConfig,
    private val tiffConfig: TiffWriterConfig
) {

    /**
     * Convert PDF file to TIFF file
     *
     * @param inputFile Input PDF file
     * @param outputFile Output TIFF file
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
     * Convert PDF InputStream to TIFF OutputStream
     *
     * @param inputStream Input PDF InputStream
     * @param outputStream Output TIFF OutputStream
     */
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        // Simplified conversion implementation
        val reader = PdfImageReader(inputStream, pdfConfig)
        val writer = TiffWriter(outputStream, tiffConfig)
        
        // Read PDF pages as flow and write to TIFF
        writer.write(reader.read())
    }

    /**
     * Convert PDF file to TIFF file using string paths
     *
     * @param inputPath Input PDF file path
     * @param outputPath Output TIFF file path
     */
    suspend fun convert(inputPath: String, outputPath: String) {
        val inputFile = File(inputPath)
        val outputFile = File(outputPath)
        convert(inputFile, outputFile)
    }
}

/**
 * Extension function: Convert File to TIFF with DSL configuration
 *
 * @param outputFile Output TIFF file
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun File.toTiff(
    outputFile: File,
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, outputFile)
}

/**
 * Extension function: Convert File to TIFF with output stream
 *
 * @param outputStream Output TIFF stream
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun File.toTiff(
    outputStream: OutputStream,
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this.inputStream(), outputStream)
}

/**
 * Extension function: Convert File to TIFF with string path
 *
 * @param outputPath Output TIFF file path
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun File.toTiff(
    outputPath: String,
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, File(outputPath))
}

/**
 * Extension function: Convert InputStream to TIFF with output file
 *
 * @param outputFile Output TIFF file
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun InputStream.toTiff(
    outputFile: File,
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, outputFile.outputStream())
}

/**
 * Extension function: Convert InputStream to TIFF with output stream
 *
 * @param outputStream Output TIFF stream
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun InputStream.toTiff(
    outputStream: OutputStream,
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, outputStream)
}

/**
 * Extension function: Convert InputStream to TIFF with string path
 *
 * @param outputPath Output TIFF file path
 * @param pdfConfig PDF reading configuration
 * @param tiffConfig TIFF writing configuration
 */
suspend fun InputStream.toTiff(
    outputPath: String,
    pdfConfig: PdfImageReaderConfig.() -> Unit = {},
    tiffConfig: TiffWriterConfig.() -> Unit = {}
) {
    pdfToTiff(pdfConfig, tiffConfig).convert(this, File(outputPath).outputStream())
}