package dev.waylon.apexflow.pdf2tiff

import dev.waylon.apexflow.conversion.pdfToTiff
import dev.waylon.apexflow.conversion.tiffToPdf
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Modern benchmark tests for PDF to TIFF conversion using Kotlin Time API
 *
 * Tests the performance of various conversion scenarios with precise timing measurements
 */
@OptIn(ExperimentalTime::class)
class PdfToTiffBenchmarkTest {

    /**
     * Test performance of large PDF to TIFF conversion with precise timing
     */
    @Test
    fun `benchmark large pdf to tiff conversion`() = runBlocking {
        val pdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
        if (!pdfFile.exists()) return@runBlocking

        val outputFile = File("build/benchmark-large-pdf.tiff")

        // Measure conversion time with Kotlin Time API
        val duration: Duration = measureTime {
            pdfToTiff().convert(pdfFile, outputFile)
        }

        println("PDF to TIFF Conversion - Large File: ${duration.inWholeMilliseconds}ms")

        // Clean up
        outputFile.delete()
    }

    /**
     * Benchmark different DPI settings for PDF to TIFF conversion
     */
    @Test
    fun `benchmark different dpi settings`() = runBlocking {
        val pdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
        if (!pdfFile.exists()) return@runBlocking

        // Test with various DPI settings
        val dpiSettings = listOf(72f, 150f, 300f)

        dpiSettings.forEach { dpi ->
            val outputFile = File("build/benchmark-dpi-${dpi}.tiff")

            val duration: Duration = measureTime {
                pdfToTiff(
                    pdfConfig = {
                        this.dpi = dpi
                    }
                ).convert(pdfFile, outputFile)
            }

            println("PDF to TIFF Conversion - DPI $dpi: ${duration.inWholeMilliseconds}ms")

            outputFile.delete()
        }
    }

    /**
     * Benchmark TIFF to PDF conversion
     */
    @Test
    fun `benchmark tiff to pdf conversion`() = runBlocking {
        val tiffFile = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")
        if (!tiffFile.exists()) return@runBlocking

        val outputFile = File("build/benchmark-tiff-to-pdf.pdf")

        val duration: Duration = measureTime {
            tiffToPdf().convert(tiffFile, outputFile)
        }

        println("TIFF to PDF Conversion: ${duration.inWholeMilliseconds}ms")

        outputFile.delete()
    }

    /**
     * Benchmark InputStream to OutputStream conversion
     */
    @Test
    fun `benchmark stream to stream conversion`() = runBlocking {
        val pdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
        if (!pdfFile.exists()) return@runBlocking

        val duration: Duration = measureTime {
            pdfFile.inputStream().use { inputStream ->
                File("build/benchmark-stream.tiff").outputStream().use { outputStream ->
                    pdfToTiff().convert(inputStream, outputStream)
                }
            }
        }

        println("Stream to Stream Conversion: ${duration.inWholeMilliseconds}ms")

        File("build/benchmark-stream.tiff").delete()
    }
}
