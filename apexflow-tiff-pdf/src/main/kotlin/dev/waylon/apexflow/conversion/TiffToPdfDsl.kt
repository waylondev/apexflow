package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.pdf.PdfImageWriter
import dev.waylon.apexflow.pdf.PdfImageWriterConfig
import dev.waylon.apexflow.tiff.TiffReader
import dev.waylon.apexflow.tiff.TiffReaderConfig
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.withTiming

/**
 * TIFF to PDF conversion DSL
 * 
 * Provides a fluent API for converting TIFF files to PDF format
 * with support for file-based input/output and flexible configurations.
 * 
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * tiffToPdf(
 *     tiffConfig = { /* TIFF config */ },
 *     pdfConfig = { jpegQuality = 90f }
 * ).convert(inputFile, outputFile)
 * 
 * // Convert from String path to String path with default settings
 * tiffToPdf().convert("input.tiff", "output.pdf")
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
        // Use constructor with config object
        val imagesFlow = TiffReader(inputFile, tiffConfig)
            .read()
            .withTiming("dev.waylon.apexflow.tiff.reader")
            .flowOn(Dispatchers.IO)
        
        PdfImageWriter(outputFile, pdfConfig)
            .write(imagesFlow)
    }
    
    /**
     * Convert TIFF InputStream to PDF OutputStream
     * 
     * @param inputStream Input TIFF InputStream
     * @param outputStream Output PDF OutputStream
     */
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        // Use constructor with config object
        val imagesFlow = TiffReader(inputStream, tiffConfig)
            .read()
            .withTiming("dev.waylon.apexflow.tiff.reader")
            .flowOn(Dispatchers.IO)
        
        PdfImageWriter(outputStream, pdfConfig)
            .write(imagesFlow)
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