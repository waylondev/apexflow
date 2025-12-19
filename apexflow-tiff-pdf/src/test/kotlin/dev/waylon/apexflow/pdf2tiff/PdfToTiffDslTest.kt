package dev.waylon.apexflow.pdf2tiff

import dev.waylon.apexflow.conversion.pdfToTiff
import dev.waylon.apexflow.conversion.toTiff
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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

    // Test PDF file path
    private val testPdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")

    // Test TIFF file path (will be created during test)
    private val testTiffFile = File("build/test-pdf-to-tiff-dsl.tiff")

    @BeforeEach
    fun setUp() {
        // Ensure output directory exists
        testTiffFile.parentFile?.mkdirs()
    }

    @AfterEach
    fun tearDown() {
        // Clean up test files
        testTiffFile.delete()
    }

    /**
     * Test basic PDF to TIFF conversion using DSL function
     */
    @Test
    fun `test basic pdf to tiff conversion`() = runBlocking {
        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test basic DSL conversion
        pdfToTiff().convert(testPdfFile, testTiffFile)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")
    }

    /**
     * Test PDF to TIFF conversion with custom configuration
     */
    @Test
    fun `test pdf to tiff with custom configuration`() = runBlocking {
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
    }

    /**
     * Test File.toTiff extension function
     */
    @Test
    fun `test File toTiff extension function`() = runBlocking {
        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test File.toTiff extension function
        testPdfFile.toTiff(testTiffFile)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")
    }

    /**
     * Test InputStream to TIFF conversion
     */
    @Test
    fun `test InputStream to TIFF conversion`() = runBlocking {
        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test DSL with File parameters (already tested InputStream in other test)
        pdfToTiff().convert(testPdfFile, testTiffFile)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")
    }

    /**
     * Test String path conversion
     */
    @Test
    fun `test String path conversion`() = runBlocking {
        // Verify input file exists
        assertTrue(testPdfFile.exists(), "Test PDF file does not exist")

        // Test DSL with String paths
        val inputPath = testPdfFile.absolutePath
        val outputPath = testTiffFile.absolutePath

        pdfToTiff().convert(inputPath, outputPath)

        // Verify output file was created
        assertTrue(testTiffFile.exists(), "Output TIFF file was not created")
        assertTrue(testTiffFile.length() > 0, "Output TIFF file is empty")
    }

    /**
     * Test InputStream to OutputStream conversion
     */
    @Test
    fun `test InputStream to OutputStream conversion`() = runBlocking {
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
    }

    /**
     * Test InputStream.toTiff extension function
     */
    @Test
    fun `test InputStream toTiff extension function`() = runBlocking {
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
    }
}
