package dev.waylon.apexflow.conversion

import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Test file for PDF to TIFF conversion DSL
 *
 * Tests all PDF to TIFF conversion functionality including:
 * - Basic PDF to TIFF conversion
 * - Conversion with custom configuration
 * - Different input/output type combinations
 * - Extension functions for File and InputStream
 */
class PdfToTiffDslTest {

    private val logger = LoggerFactory.getLogger(PdfToTiffDslTest::class.java)

    // Test PDF file path
    private val testPdfFile = File("build/test-pdf-to-tiff-dsl.pdf")

    // Test TIFF file path (will be created during test)
    private val testTiffFile = File("build/test-pdf-to-tiff-dsl.tiff")

    /**
     * Test basic PDF to TIFF conversion using DSL function
     */
    @Test
    fun `test basic pdf to tiff conversion`() = runBlocking {
        logger.info("=== Test: Basic PDF to TIFF conversion ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test basic DSL conversion
        pdfToTiff().convert(testPdfFile, testTiffFile)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")

        logger.info("✓ Basic PDF to TIFF conversion successful")
    }

    /**
     * Test PDF to TIFF conversion with custom configuration
     */
    @Test
    fun `test pdf to tiff with custom configuration`() = runBlocking {
        logger.info("=== Test: PDF to TIFF with custom configuration ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test DSL with custom configuration
        pdfToTiff(
            pdfConfig = {
                dpi = 150f
                skipBlankPages = true
            },
            tiffConfig = {
                compressionType = "LZW"
                compressionQuality = 100f
            }
        ).convert(testPdfFile, testTiffFile)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")

        logger.info("✓ PDF to TIFF with custom configuration successful")
    }

    /**
     * Test File.toTiff extension function
     */
    @Test
    fun `test File toTiff extension function`() = runBlocking {
        logger.info("=== Test: File.toTiff extension function ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test File.toTiff extension function
        testPdfFile.toTiff(testTiffFile)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")

        logger.info("✓ File.toTiff extension function successful")
    }

    /**
     * Test InputStream to TIFF conversion
     */
    @Test
    fun `test InputStream to TIFF conversion`() = runBlocking {
        logger.info("=== Test: InputStream to TIFF conversion ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test DSL with File parameters (already tested InputStream in other test)
        pdfToTiff().convert(testPdfFile, testTiffFile)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")

        logger.info("✓ InputStream to TIFF conversion successful")
    }

    /**
     * Test String path conversion
     */
    @Test
    fun `test String path conversion`() = runBlocking {
        logger.info("=== Test: String path conversion ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test DSL with String paths
        val inputPath = testPdfFile.absolutePath
        val outputPath = testTiffFile.absolutePath

        pdfToTiff().convert(inputPath, outputPath)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")

        logger.info("✓ String path conversion successful")
    }

    /**
     * Test InputStream to OutputStream conversion
     */
    @Test
    fun `test InputStream to OutputStream conversion`() = runBlocking {
        logger.info("=== Test: InputStream to OutputStream conversion ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Create byte array output stream to capture the result
        val outputStream = ByteArrayOutputStream()

        // Create input stream from file
        testPdfFile.inputStream().use { inputStream ->
            // Test DSL with InputStream and OutputStream
            pdfToTiff().convert(inputStream, outputStream)
        }

        // Verify output stream has data
        assertTrue(outputStream.size() > 0, "Output stream is empty")

        logger.info("✓ InputStream to OutputStream conversion successful")
    }

    /**
     * Test InputStream.toTiff extension function
     */
    @Test
    fun `test InputStream toTiff extension function`() = runBlocking {
        logger.info("=== Test: InputStream.toTiff extension function ===")

        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Create input stream from file
        testPdfFile.inputStream().use { inputStream ->
            // Test InputStream.toTiff extension function
            inputStream.toTiff(testTiffFile)
        }

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")

        logger.info("✓ InputStream.toTiff extension function successful")
    }

    /**
     * Clean up test files after all tests
     */
//    @Test
    fun `clean up test files`() {
        logger.info("=== Test: Clean up test files ===")

        // Delete temporary files if they exist
        if (testTiffFile.exists()) {
            testTiffFile.delete()
            logger.info("✓ Deleted test TIFF file: ${testTiffFile.absolutePath}")
        }

        logger.info("✓ Clean up completed")
    }
}
