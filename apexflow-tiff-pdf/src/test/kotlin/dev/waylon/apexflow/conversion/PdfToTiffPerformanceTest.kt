package dev.waylon.apexflow.conversion

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Performance tests for PDF to TIFF conversion
 *
 * Tests the performance of various conversion scenarios including:
 * - Large file conversion
 * - Conversion with different configurations
 * - Batch processing
 */
class PdfToTiffPerformanceTest {

    private val logger = LoggerFactory.getLogger(PdfToTiffPerformanceTest::class.java)

    /**
     * Test performance of large PDF to TIFF conversion
     */
    @Test
    fun `test large pdf to tiff conversion performance`() = runBlocking {
        logger.info("=== Performance Test: Large PDF to TIFF conversion ===")

        // Use a larger PDF file for performance testing
        val largePdfFile = File("dist/spring-boot-reference-406.pdf")
        if (!largePdfFile.exists()) {
            logger.warn("Large test PDF not found, skipping performance test")
            return@runBlocking
        }

        val outputFile = File.createTempFile("large-pdf", ".tiff")

        try {
            logger.info("Starting conversion of large PDF file (${largePdfFile.length() / 1024 / 1024} MB)")
            
            // Record start time
            val startTime = System.currentTimeMillis()

            // Convert with default settings
            pdfToTiff().convert(largePdfFile, outputFile)

            // Calculate duration
            val duration = System.currentTimeMillis() - startTime
            
            logger.info("✓ Large PDF to TIFF conversion completed in ${duration}ms")
            logger.info("  Output file size: ${outputFile.length() / 1024 / 1024} MB")
            logger.info("  Conversion speed: ${(largePdfFile.length() / 1024) / (duration / 1000.0)} KB/s")
            
        } finally {
            // Clean up
            outputFile.delete()
        }
    }

    /**
     * Test performance with different DPI settings
     */
    @Test
    fun `test performance with different dpi settings`() = runBlocking {
        logger.info("=== Performance Test: Different DPI settings ===")

        val testPdfFile = File("dist/test-pdf-to-tiff-dsl.pdf")
        if (!testPdfFile.exists()) {
            logger.warn("Test PDF not found, skipping DPI performance test")
            return@runBlocking
        }

        // Test with various DPI settings
        val dpiSettings = listOf(72f, 150f, 300f)
        
        dpiSettings.forEach { dpi ->
            val outputFile = File.createTempFile("dpi-test-${dpi}", ".tiff")
            
            try {
                logger.info("Testing with DPI: ${dpi}")
                
                val startTime = System.currentTimeMillis()
                
                pdfToTiff(
                    pdfConfig = {
                        this.dpi = dpi
                    }
                ).convert(testPdfFile, outputFile)
                
                val duration = System.currentTimeMillis() - startTime
                
                logger.info("  ✓ DPI ${dpi}: ${duration}ms, Output size: ${outputFile.length() / 1024} KB")
                
            } finally {
                outputFile.delete()
            }
        }
    }

    /**
     * Test performance of TIFF to PDF conversion
     */
    @Test
    fun `test tiff to pdf conversion performance`() = runBlocking {
        logger.info("=== Performance Test: TIFF to PDF conversion ===")

        // Use a larger TIFF file for performance testing
        val largeTiffFile = File("dist/test-tiff-to-pdf-dsl.tiff")
        if (!largeTiffFile.exists()) {
            logger.warn("Large test TIFF not found, skipping performance test")
            return@runBlocking
        }

        val outputFile = File.createTempFile("large-tiff", ".pdf")

        try {
            logger.info("Starting conversion of large TIFF file (${largeTiffFile.length() / 1024 / 1024} MB)")
            
            // Record start time
            val startTime = System.currentTimeMillis()

            // Convert with default settings
            tiffToPdf().convert(largeTiffFile, outputFile)

            // Calculate duration
            val duration = System.currentTimeMillis() - startTime
            
            logger.info("✓ Large TIFF to PDF conversion completed in ${duration}ms")
            logger.info("  Output file size: ${outputFile.length() / 1024 / 1024} MB")
            logger.info("  Conversion speed: ${(largeTiffFile.length() / 1024) / (duration / 1000.0)} KB/s")
            
        } finally {
            // Clean up
            outputFile.delete()
        }
    }
}
