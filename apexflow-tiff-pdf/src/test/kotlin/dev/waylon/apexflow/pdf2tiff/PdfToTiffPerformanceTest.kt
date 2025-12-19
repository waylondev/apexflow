package dev.waylon.apexflow.conversion

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Performance tests for PDF to TIFF conversion
 *
 * Tests the performance of various conversion scenarios including:
 * - Large file conversion
 * - Conversion with different configurations
 */
class PdfToTiffPerformanceTest {

    /**
     * Test performance of large PDF to TIFF conversion
     */
    @Test
    fun `test large pdf to tiff conversion performance`() = runBlocking {
        // Use a larger PDF file for performance testing
        val largePdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
        if (!largePdfFile.exists()) {
            return@runBlocking
        }

        val outputFile = File("build/large-pdf.tiff")

        // Convert with default settings
        pdfToTiff().convert(largePdfFile, outputFile)

        // Clean up
        outputFile.delete()
    }

    /**
     * Test performance with different DPI settings
     */
    @Test
    fun `test performance with different dpi settings`() = runBlocking {
        val testPdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
        if (!testPdfFile.exists()) {
            return@runBlocking
        }

        // Test with various DPI settings
        val dpiSettings = listOf(72f, 150f, 300f)

        dpiSettings.forEach { dpi ->
            val outputFile = File.createTempFile("dpi-test-${dpi}", ".tiff")

            // Convert with specified DPI
            pdfToTiff(
                pdfConfig = {
                    this.dpi = dpi
                }
            ).convert(testPdfFile, outputFile)

            // Clean up using use block for automatic resource management
            outputFile.delete()
        }
    }

    /**
     * Test performance of TIFF to PDF conversion
     */
    @Test
    fun `test tiff to pdf conversion performance`() = runBlocking {
        // Use a larger TIFF file for performance testing
        val largeTiffFile = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")
        if (!largeTiffFile.exists()) {
            return@runBlocking
        }

        val outputFile = File("build/large-tiff.pdf")

        // Convert with default settings
        tiffToPdf().convert(largeTiffFile, outputFile)

        // Clean up
        outputFile.delete()
    }
}
