package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageReaderConfig
import dev.waylon.apexflow.tiff.TiffWriter
import dev.waylon.apexflow.tiff.TiffWriterConfig
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.withTiming

/**
 * PDF to TIFF conversion DSL
 * 
 * Provides a fluent API for converting PDF files to TIFF format
 * with support for file-based input/output and flexible configurations.
 * 
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * pdfToTiff(
 *     pdfConfig = { dpi = 150f },
 *     tiffConfig = { compressionType = "JPEG" }
 * ).convert(inputFile, outputFile)
 * 
 * // Convert from String path to String path with default settings
 * pdfToTiff().convert("input.pdf", "output.tiff")
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
        // Use constructor with config object
        val imagesFlow = PdfImageReader(inputFile, pdfConfig)
            .read()
            .withTiming("dev.waylon.apexflow.pdf.reader")
            .flowOn(Dispatchers.IO)
        
        TiffWriter(outputFile, tiffConfig)
            .write(imagesFlow)
    }
    
    /**
     * Convert PDF InputStream to TIFF OutputStream
     * 
     * @param inputStream Input PDF InputStream
     * @param outputStream Output TIFF OutputStream
     */
    suspend fun convert(inputStream: InputStream, outputStream: OutputStream) {
        // Use constructor with config object
        val imagesFlow = PdfImageReader(inputStream, pdfConfig)
            .read()
            .withTiming("dev.waylon.apexflow.pdf.reader")
            .flowOn(Dispatchers.IO)
        
        TiffWriter(outputStream, tiffConfig)
            .write(imagesFlow)
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