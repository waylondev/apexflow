package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.withPluginPerformanceMonitoring
import dev.waylon.apexflow.pdf.ApexPdfReader
import dev.waylon.apexflow.pdf.PdfConfig
import dev.waylon.apexflow.tiff.ApexTiffWriter
import dev.waylon.apexflow.tiff.TiffConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Separate performance tests for PDF reading and TIFF writing
 * Using ApexFlow performance monitoring plugin
 */
class PdfTiffSeparatePerformanceTest {

    private val testPdf = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
    private val testTiff = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")

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
        
        // Create PDF reader
        val pdfReader = ApexPdfReader.fromFile(testPdf, pdfConfig)
        
        // Create a workflow that only reads PDF
        val readWorkflow = apexFlow<Unit, BufferedImage> {
            transform { input ->
                pdfReader.transform(input)
            }
        }
        
        // Add performance monitoring
        val monitoredWorkflow = readWorkflow.withPluginPerformanceMonitoring(
            loggerName = "dev.waylon.apexflow.performance.pdf.reading",
            samplingIntervalMs = 1000,
            enableDetailedMetrics = true
        )
        
        // Measure total time
        val totalTime = measureTimeMillis {
            monitoredWorkflow.execute(flowOf(Unit)).collect()
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
        
        // First, read some images to write
        val pdfConfig = PdfConfig().apply {
            dpi = 150f
        }
        val pdfReader = ApexPdfReader.fromFile(testPdf, pdfConfig)
        val images = pdfReader.transform(flowOf(Unit)).toList()
        println("Prepared ${images.size} images for TIFF writing")
        
        val tiffConfig = TiffConfig().apply {
            compressionType = "JPEG"
            compressionQuality = 70f
            bufferSize = 0 // No buffering for accurate measurement
        }
        
        // Create TIFF writer to ByteArrayOutputStream (no disk I/O)
        val outputStream = ByteArrayOutputStream()
        val tiffWriter = ApexTiffWriter.toOutputStream(outputStream, tiffConfig)
        
        // Create a workflow that only writes TIFF
        val writeWorkflow = apexFlow<List<BufferedImage>, Unit> {
            transform { input ->
                // Flatten list to Flow<BufferedImage>
                input.flatMapMerge { imageList ->
                    imageList.asFlow()
                }.let { imageFlow ->
                    tiffWriter.transform(imageFlow)
                }
            }
        }
        
        // Add performance monitoring
        val monitoredWorkflow = writeWorkflow.withPluginPerformanceMonitoring(
            loggerName = "dev.waylon.apexflow.performance.tiff.writing",
            samplingIntervalMs = 1000,
            enableDetailedMetrics = true
        )
        
        // Measure total time
        val totalTime = measureTimeMillis {
            monitoredWorkflow.execute(flowOf(images)).collect()
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
            pdfToTiff(
                pdfConfig = pdfConfig,
                tiffConfig = tiffConfig
            ).convert(testPdf, File.createTempFile("test-output", ".tiff").apply { deleteOnExit() })
        }
        
        println("Total Combined Conversion Time: ${totalTime} ms")
        println("==================================")
    }
}