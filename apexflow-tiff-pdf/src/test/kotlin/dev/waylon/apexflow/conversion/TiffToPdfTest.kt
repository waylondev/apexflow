package dev.waylon.apexflow.conversion

import java.io.File
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Performance tests for PDF and TIFF conversion
 *
 * These tests focus on optimal performance configurations
 * for converting large files using ApexFlow components.
 */
class TiffToPdfTest {
    val inputTiff = File("dist/test-tiff-to-pdf-dsl.tiff")
    val outputPdf = File("build/test-tiff-to-pdf-dsl-output.pdf")


    /**
     * Test optimal TIFF to PDF conversion performance
     * using extension functions for simplified code
     */
    @Test
    fun `test optimal tiff to pdf performance`() = runBlocking {
        // Use existing test TIFF

        println("Starting optimal TIFF to PDF conversion test...")

        // Measure conversion time
        val timeMillis = measureTimeMillis {
            // Use extension function for simplified call
            inputTiff.toPdf(
                outputPdf,
                tiffConfig = {
                    // TIFF config with optimal settings
                    // Empty config uses defaults, which are optimal for performance
                    bufferSize = 64
                },
                pdfConfig = {
                    // PDF config with optimal settings
                    jpegQuality = 0.7f // Balanced quality for speed
                }
            )
        }

        println("TIFF to PDF conversion completed in $timeMillis ms")
        println("Output file size: ${outputPdf.length() / 1024 / 1024} MB")

        // Clean up
//        outputPdf.delete()
    }
}