package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.withPluginPerformanceMonitoring
import dev.waylon.apexflow.pdf.ApexPdfReader
import dev.waylon.apexflow.pdf.PdfConfig
import dev.waylon.apexflow.tiff.ApexTiffWriter
import dev.waylon.apexflow.tiff.TiffConfig
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Separate performance tests for PDF reading and TIFF writing
 * Using ApexFlow performance monitoring plugin
 */
class PdfTiffSeparatePerformanceTest {

    private val testPdf = File("dist/spring-boot-reference-406.pdf")

    /**
     * Test PDF reading performance only with performance monitoring
     */
    @Test
    fun `test pdf reading performance with monitoring`() = runBlocking {
        println("=== PDF READING PERFORMANCE TEST ===")

        val pdfConfig = PdfConfig().apply {
            dpi = 150f
            bufferSize = 0 // No buffering for accurate measurement
        }

        // Create PDF reader with performance monitoring
        val pdfReader = ApexPdfReader.fromFile(testPdf, pdfConfig)
            .withPluginPerformanceMonitoring(
                loggerName = "dev.waylon.apexflow.performance.pdf.reading",
                samplingIntervalMs = 1000,
                enableDetailedMetrics = true
            )

        // Measure total time - execute directly with Unit input
        val totalTime = measureTimeMillis {
            pdfReader.execute(Unit).collect()
        }

        println("Total PDF Reading Time: ${totalTime} ms")
        println("==================================")
    }

    /**
     * Test TIFF writing performance only with performance monitoring
     */
    @Test
    fun `test tiff writing performance with monitoring`() = runBlocking {
        println("=== TIFF WRITING PERFORMANCE TEST ===")

        // First, read some images to write (using separate reader without monitoring)
        val pdfConfig = PdfConfig().apply {
            dpi = 150f
        }
        val pdfReader = ApexPdfReader.fromFile(testPdf, pdfConfig)
        val images = pdfReader.execute(Unit).toList()
        println("Prepared ${images.size} images for TIFF writing")

        val tiffConfig = TiffConfig().apply {
            compressionType = "JPEG"
            compressionQuality = 70f
            bufferSize = 0 // No buffering for accurate measurement
        }

        // Create TIFF writer to ByteArrayOutputStream (no disk I/O) with performance monitoring
        val outputStream = ByteArrayOutputStream()
        val tiffWriter = ApexTiffWriter.toOutputStream(outputStream, tiffConfig)
            .withPluginPerformanceMonitoring(
                loggerName = "dev.waylon.apexflow.performance.tiff.writing",
                samplingIntervalMs = 1000,
                enableDetailedMetrics = true
            )

        // Measure total time - execute directly with images
        val totalTime = measureTimeMillis {
            tiffWriter.execute(images).collect()
        }

        println("Total TIFF Writing Time: ${totalTime} ms")
        println("Output size: ${outputStream.size() / 1024 / 1024} MB")
        println("==================================")
    }

    /**
     * Test combined PDF to TIFF conversion with performance monitoring
     */
    @Test
    fun `test combined pdf to tiff performance with monitoring`() = runBlocking {
        println("=== COMBINED PDF TO TIFF PERFORMANCE TEST ===")

        // Use same configuration for fair comparison
        val pdfConfig = PdfConfig().apply {
            dpi = 150f
            bufferSize = 0
        }

        val tiffConfig = TiffConfig().apply {
            compressionType = "JPEG"
            compressionQuality = 70f
            bufferSize = 0
        }

        // Test with our DSL which uses proper flow composition
        val totalTime = measureTimeMillis {
            apexPdfToTiff(
                pdfConfig = {
                    dpi = 150f
                    bufferSize = 0
                },
                tiffConfig = {
                    compressionType = "JPEG"
                    compressionQuality = 70f
                    bufferSize = 0
                }
            ).convert(testPdf, File.createTempFile("test-output", ".tiff").apply { deleteOnExit() })
        }

        println("Total Combined Conversion Time: ${totalTime} ms")
        println("==================================")
    }
}