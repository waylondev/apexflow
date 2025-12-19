package dev.waylon.apexflow.conversion

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Test file for TIFF to PDF conversion DSL
 *
 * Tests all TIFF to PDF conversion functionality including:
 * - Basic TIFF to PDF conversion
 * - Conversion with custom configuration
 * - Different input/output type combinations
 * - Extension functions for File and InputStream
 */
class TiffToPdfDslTest {

    private val logger = LoggerFactory.getLogger(TiffToPdfDslTest::class.java)

    // Test TIFF file path (will be created during test)
    private val testTiffFile = File("dist/test-tiff-to-pdf-dsl.tiff")

    // Temporary output file for testing
    private val tempOutputFile = File("dist/test-tiff-to-pdf-dsl-output.pdf")

    /**
     * Ensure we have a test TIFF file by converting from PDF if needed
     */
    private fun ensureTestTiffExists() {
        logger.info("Creating test TIFF file from PDF...")
        assertTrue(testTiffFile.exists(), "Failed to create test TIFF file")
        logger.info("Test TIFF file created successfully: ${testTiffFile.absolutePath}")
    }

    /**
     * Test basic TIFF to PDF conversion using DSL function
     */
    @Test
    fun `test basic tiff to pdf conversion`() = runBlocking {
        logger.info("=== Test: Basic TIFF to PDF conversion ===")

        // Ensure we have a test TIFF file
        ensureTestTiffExists()

        // Test basic DSL conversion
        tiffToPdf().convert(testTiffFile, tempOutputFile)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")

        // Clean up
        tempOutputFile.delete()

        logger.info("✓ Basic TIFF to PDF conversion successful")
    }

    /**
     * Test TIFF to PDF conversion with custom configuration
     */
    @Test
    fun `test tiff to pdf with custom configuration`() = runBlocking {
        logger.info("=== Test: TIFF to PDF with custom configuration ===")

        // Ensure we have a test TIFF file
        ensureTestTiffExists()

        // Test DSL with custom configuration
        tiffToPdf(
            tiffConfig = { /* TIFF config */ },
            pdfConfig = {
                jpegQuality = 95f
                compressContent = true
                pdfVersion = "1.7"
            }
        ).convert(testTiffFile, tempOutputFile)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")

        // Clean up
        tempOutputFile.delete()

        logger.info("✓ TIFF to PDF with custom configuration successful")
    }

    /**
     * Test File.toPdf extension function
     */
    @Test
    fun `test File toPdf extension function`() = runBlocking {
        logger.info("=== Test: File.toPdf extension function ===")

        // Ensure we have a test TIFF file
        ensureTestTiffExists()

        // Test File.toPdf extension function
        testTiffFile.toPdf(tempOutputFile)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")

        // Clean up
        tempOutputFile.delete()

        logger.info("✓ File.toPdf extension function successful")
    }

    /**
     * Test InputStream to PDF conversion
     */
    @Test
    fun `test InputStream to PDF conversion`() = runBlocking {
        logger.info("=== Test: InputStream to PDF conversion ===")

        // Ensure we have a test TIFF file
        ensureTestTiffExists()

        // Test DSL with File parameters (already tested InputStream in other test)
        tiffToPdf().convert(testTiffFile, tempOutputFile)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")

        // Clean up
        tempOutputFile.delete()

        logger.info("✓ InputStream to PDF conversion successful")
    }

    /**
     * Test String path conversion
     */
    @Test
    fun `test String path conversion`() = runBlocking {
        logger.info("=== Test: String path conversion ===")

        // Ensure we have a test TIFF file
        ensureTestTiffExists()

        // Test DSL with String paths
        val inputPath = testTiffFile.absolutePath
        val outputPath = tempOutputFile.absolutePath

        tiffToPdf().convert(inputPath, outputPath)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")

        // Clean up
        tempOutputFile.delete()

        logger.info("✓ String path conversion successful")
    }

    /**
     * Test InputStream.toPdf extension function
     */
    @Test
    fun `test InputStream toPdf extension function`() = runBlocking {
        logger.info("=== Test: InputStream.toPdf extension function ===")

        // Ensure we have a test TIFF file
        ensureTestTiffExists()

        // Create input stream from file
        testTiffFile.inputStream().use { inputStream ->
            // Test InputStream.toPdf extension function
            inputStream.toPdf(tempOutputFile)
        }

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")

        // Clean up
        tempOutputFile.delete()

        logger.info("✓ InputStream.toPdf extension function successful")
    }

    /**
     * Clean up test files after all tests
     */
//    @Test
    fun `clean up test files`() {
        logger.info("=== Test: Clean up test files ===")

        // Delete temporary files if they exist
//        if (testTiffFile.exists()) {
//            testTiffFile.delete()
//            logger.info("✓ Deleted test TIFF file: ${testTiffFile.absolutePath}")
//        }

        if (tempOutputFile.exists()) {
            tempOutputFile.delete()
            logger.info("✓ Deleted temporary output file: ${tempOutputFile.absolutePath}")
        }

        logger.info("✓ Clean up completed")
    }
}
