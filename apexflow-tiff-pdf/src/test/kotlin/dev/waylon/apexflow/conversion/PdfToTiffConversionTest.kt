package dev.waylon.apexflow.conversion

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Test case for converting a specific PDF file to TIFF
 *
 * This test converts the spring-boot-reference-3.3.0-977.pdf file from the build directory to TIFF
 */
class PdfToTiffConversionTest {

    private val logger = LoggerFactory.getLogger(PdfToTiffConversionTest::class.java)

    /**
     * Test converting spring-boot-reference-3.3.0-977.pdf to TIFF
     *
     * This test demonstrates the core ApexFlow design: "Everything is Flow"
     * with multiple transform steps and different coroutine dispatchers.
     *
     * Steps:
     * 1. Read PDF file (IO intensive)
     * 2. Parse PDF to get image flow (CPU intensive)
     * 3. Process images (CPU intensive)
     * 4. Write TIFF file (IO intensive)
     */
    @Test
    fun `test converting spring-boot-reference pdf to tiff`() = runBlocking {
        // Define input and output file paths
        val inputPdf = File("dist/test-pdf-to-tiff-dsl.pdf")
        val outputTiff = File("dist/test-pdf-to-tiff-dsl.tiff")

        // Check if input file exists
        assertTrue(inputPdf.exists(), "Input PDF file does not exist: ${inputPdf.absolutePath}")

        logger.info("Converting PDF to TIFF...")
        logger.info("Input: {}", inputPdf.absolutePath)
        logger.info("Output: {}", outputTiff.absolutePath)
        logger.info("Input size: {} KB", inputPdf.length() / 1024)

        // Use the new simplified PDF to TIFF conversion DSL
        pdfToTiff(
            pdfConfig = { dpi = 100f },
            tiffConfig = {
                compressionType = "JPEG"
                compressionQuality = 90f
            }
        ).convert(inputPdf, outputTiff)

        // Verify that output file was created
        assertTrue(outputTiff.exists(), "Output TIFF file was not created")
        assertTrue(outputTiff.length() > 0, "Output TIFF file is empty")

        logger.info("Conversion completed successfully!")
        logger.info("Output size: {} KB", outputTiff.length() / 1024)
    }
}