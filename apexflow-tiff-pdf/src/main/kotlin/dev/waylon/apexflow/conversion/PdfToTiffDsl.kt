package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.core.dsl.withLogging
import dev.waylon.apexflow.core.dsl.withTiming
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageReaderConfig
import dev.waylon.apexflow.tiff.TiffWriter
import dev.waylon.apexflow.tiff.TiffWriterConfig
import java.awt.image.BufferedImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList

/**
 * PDF to TIFF conversion configuration
 */
class PdfToTiffConfig {
    /**
     * PDF reading configuration
     */
    val pdfConfig = PdfImageReaderConfig()
    
    /**
     * TIFF writing configuration
     */
    val tiffConfig = TiffWriterConfig()
    
    /**
     * Configure PDF reading settings
     */
    fun pdf(config: PdfImageReaderConfig.() -> Unit) {
        pdfConfig.apply(config)
    }
    
    /**
     * Configure TIFF writing settings
     */
    fun tiff(config: TiffWriterConfig.() -> Unit) {
        tiffConfig.apply(config)
    }
}

/**
 * PDF to TIFF conversion DSL
 * 
 * Provides a fluent API for converting PDF files to TIFF format
 * with support for file-based input/output and flexible configurations.
 * 
 * Usage examples:
 * ```kotlin
 * // Convert from File to File with custom settings
 * pdfToTiff {
 *     pdf { dpi = 150f }
 *     tiff { compressionType = "JPEG" }
 * }.convert(inputFile, outputFile)
 * 
 * // Convert from String path to String path with default settings
 * pdfToTiff().convert("input.pdf", "output.tiff")
 * ```
 */
fun pdfToTiff(config: PdfToTiffConfig.() -> Unit = {}): PdfToTiffConverter {
    val pdfToTiffConfig = PdfToTiffConfig().apply(config)
    return PdfToTiffConverter(pdfToTiffConfig)
}

/**
 * PDF to TIFF converter implementation
 */
class PdfToTiffConverter internal constructor(
    private val config: PdfToTiffConfig
) {
    
    /**
     * Convert PDF file to TIFF file
     * 
     * @param inputFile Input PDF file
     * @param outputFile Output TIFF file
     */
    suspend fun convert(inputFile: File, outputFile: File) {
        // Create PDF read flow (IO intensive)
        val pdfReadFlow = apexFlow<Pair<File, File>, Pair<File, Flow<BufferedImage>>> {
            transformOnIO { (pdfFile, tiffFile) ->
                val imagesFlow = PdfImageReader(pdfFile, config.pdfConfig)
                    .read()
                    .withTiming("dev.waylon.apexflow.pdf.reader")
                    .flowOn(Dispatchers.IO)
                
                Pair(tiffFile, imagesFlow)
            }
        }
        .withTiming("PDF Reading Phase")
        .withLogging("PDF Reading")
        
        // Create TIFF write flow (IO intensive)
        val tiffWriteFlow = apexFlow<Pair<File, Flow<BufferedImage>>, Unit> {
            transformOnIO { (tiffFile, imagesFlow) ->
                TiffWriter(tiffFile, config.tiffConfig)
                    .write(imagesFlow)
            }
        }
        .withTiming("TIFF Writing Phase")
        .withLogging("TIFF Writing")
        
        // Combine flows and execute
        val pdfToTiffFlow = (pdfReadFlow + tiffWriteFlow)
            .withTiming("Total PDF to TIFF Conversion")
            .withLogging("PDF to TIFF Conversion")
        
        pdfToTiffFlow.execute(inputFile to outputFile).toList()
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
