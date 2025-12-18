package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.pdf.PdfImageReaderConfig
import dev.waylon.apexflow.tiff.TiffWriter
import dev.waylon.apexflow.tiff.TiffWriterConfig
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

/**
 * Configuration for PdfToTiff conversion
 */
class PdfToTiffConfig {
    /** Configuration for PDF reading */
    var pdfReaderConfig: (PdfImageReaderConfig.() -> Unit) = {}

    /** Configuration for TIFF writing */
    var tiffWriterConfig: (TiffWriterConfig.() -> Unit) = {}
}

/**
 * ApexFlow implementation for converting PDF to TIFF
 *
 * This flow reads PDF pages from an InputStream and writes them to a TIFF OutputStream
 *
 * Usage:
 * ```kotlin
 * val pdfToTiffFlow = PdfToTiffFlow {
 *     pdfReaderConfig {
 *         dpi = 300f
 *     }
 *     tiffWriterConfig {
 *         // TIFF writer configuration
 *     }
 * }
 * ```
 */
class PdfToTiffFlow(
    private val config: PdfToTiffConfig.() -> Unit = {}
) : ApexFlow<Pair<InputStream, OutputStream>, Unit> {

    private val conversionConfig = PdfToTiffConfig().apply(config)

    override fun transform(input: Flow<Pair<InputStream, OutputStream>>): Flow<Unit> {
        return input.map { (pdfInputStream, tiffOutputStream) ->
            val pdfReader = PdfImageReader(pdfInputStream, conversionConfig.pdfReaderConfig)
            val tiffWriter = TiffWriter(tiffOutputStream, conversionConfig.tiffWriterConfig)

            val images = pdfReader.read()
            tiffWriter.write(images)
        }
    }
}

/**
 * ApexFlow implementation for converting PDF InputStream to Flow<BufferedImage>
 */
class PdfInputStreamToImagesFlow(
    private val config: PdfImageReaderConfig.() -> Unit = {}
) : ApexFlow<InputStream, BufferedImage> {

    override fun transform(input: Flow<InputStream>): Flow<BufferedImage> {
        return input.flatMapConcat { stream ->
            PdfImageReader(stream, config).read()
        }
    }
}

/**
 * ApexFlow implementation for converting Flow<BufferedImage> to TIFF OutputStream
 */
class ImagesToTiffFlow(
    private val config: TiffWriterConfig.() -> Unit = {}
) : ApexFlow<Pair<Flow<BufferedImage>, OutputStream>, Unit> {

    override fun transform(input: Flow<Pair<Flow<BufferedImage>, OutputStream>>): Flow<Unit> {
        return input.map { (images, outputStream) ->
            val tiffWriter = TiffWriter(outputStream, config)
            tiffWriter.write(images)
        }
    }
}
