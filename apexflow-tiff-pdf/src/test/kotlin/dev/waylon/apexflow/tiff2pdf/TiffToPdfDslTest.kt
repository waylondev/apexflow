package dev.waylon.apexflow.conversion

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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

    // Test TIFF file path
    private val testTiffFile = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")

    // Temporary output file for testing
    private val tempOutputFile = File("build/test-tiff-to-pdf-dsl-output.pdf")

    @BeforeEach
    fun setUp() {
        // Ensure output directory exists
        tempOutputFile.parentFile?.mkdirs()
        // Verify test TIFF file exists
        assertTrue(testTiffFile.exists(), "Test TIFF file does not exist")
    }

    @AfterEach
    fun tearDown() {
        // Clean up temporary files
        tempOutputFile.delete()
    }

    /**
     * Test basic TIFF to PDF conversion using DSL function
     */
    @Test
    fun `test basic tiff to pdf conversion`() = runBlocking {
        // Test basic DSL conversion
        tiffToPdf().convert(testTiffFile, tempOutputFile)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")
    }

    /**
     * Test TIFF to PDF conversion with custom configuration
     */
    @Test
    fun `test tiff to pdf with custom configuration`() = runBlocking {
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
    }

    /**
     * Test File.toPdf extension function
     */
    @Test
    fun `test File toPdf extension function`() = runBlocking {
        // Test File.toPdf extension function
        testTiffFile.toPdf(tempOutputFile)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")
    }

    /**
     * Test InputStream to PDF conversion
     */
    @Test
    fun `test InputStream to PDF conversion`() = runBlocking {
        // Test DSL with File parameters
        tiffToPdf().convert(testTiffFile, tempOutputFile)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")
    }

    /**
     * Test String path conversion
     */
    @Test
    fun `test String path conversion`() = runBlocking {
        // Test DSL with String paths
        val inputPath = testTiffFile.absolutePath
        val outputPath = tempOutputFile.absolutePath

        tiffToPdf().convert(inputPath, outputPath)

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")
    }

    /**
     * Test InputStream.toPdf extension function
     */
    @Test
    fun `test InputStream toPdf extension function`() = runBlocking {
        // Create input stream from file
        testTiffFile.inputStream().use { inputStream ->
            // Test InputStream.toPdf extension function
            inputStream.toPdf(tempOutputFile)
        }

        // Verify output file was created
        assertTrue(tempOutputFile.exists(), "Output PDF file was not created")
        assertTrue(tempOutputFile.length() > 0, "Output PDF file is empty")
    }
}
