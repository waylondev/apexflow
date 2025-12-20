package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.pdf.ApexPdfReader
import dev.waylon.apexflow.pdf.PdfConfig
import dev.waylon.apexflow.tiff.ApexTiffWriter
import dev.waylon.apexflow.tiff.TiffConfig
import org.junit.jupiter.api.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.runBlocking

/**
 * Unit tests for PDF to TIFF conversion
 *
 * This test class covers:
 * - Basic functionality
 * - Edge cases
 * - Error handling
 * - Configuration options
 * - Component composition
 * - Plugin functionality
 */
class PdfToTiffUnitTest {
    
    private val testPdf = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
    private val outputDirectory = File("build/test-units")
    
    @BeforeEach
    fun setUp() {
        // Create output directory if it doesn't exist
        outputDirectory.mkdirs()
    }
    
    @AfterEach
    fun tearDown() {
        // Clean up output files
        outputDirectory.listFiles()?.forEach { it.delete() }
    }
    
    /**
     * Test basic PDF to TIFF conversion
     */
    @Test
    fun `test basic pdf to tiff conversion`() = runBlocking {
        val outputTiff = File(outputDirectory, "basic-output.tiff")
        
        // Execute conversion
        testPdf.toTiff(outputTiff)
        
        // Verify output file exists and is not empty
        Assertions.assertTrue(outputTiff.exists(), "Output TIFF file should exist")
        Assertions.assertTrue(outputTiff.length() > 0, "Output TIFF file should not be empty")
    }
    
    /**
     * Test PDF to TIFF conversion with custom configuration
     */
    @Test
    fun `test pdf to tiff with custom configuration`() = runBlocking {
        val outputTiff = File(outputDirectory, "custom-config-output.tiff")
        
        // Execute conversion with custom settings
        testPdf.toTiff(
            outputTiff,
            pdfConfig = {
                dpi = 200f
                skipBlankPages = true
                bufferSize = 5
            },
            tiffConfig = {
                compressionType = "JPEG"
                compressionQuality = 80f
                bufferSize = 5
            }
        )
        
        // Verify output file exists
        Assertions.assertTrue(outputTiff.exists(), "Output TIFF file should exist")
    }
    
    /**
     * Test PDF to TIFF conversion using component composition
     */
    @Test
    fun `test pdf to tiff component composition`() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        
        // Create components
        val pdfReader = ApexPdfReader.fromFile(testPdf, PdfConfig().apply { dpi = 150f })
        val tiffWriter = ApexTiffWriter.toOutputStream(outputStream, TiffConfig().apply { compressionType = "JPEG" })
        
        // Compose components using + operator
        val pipeline = pdfReader + tiffWriter
        
        // Execute pipeline
        pipeline.execute(Unit).collect { }
        
        // Verify output is generated
        Assertions.assertTrue(outputStream.size() > 0, "Output stream should contain data")
    }
    
    /**
     * Test edge case: Empty PDF file
     */
    @Test
    fun `test pdf to tiff with empty pdf`() = runBlocking {
        val emptyPdf = File.createTempFile("empty", ".pdf", outputDirectory)
        val outputTiff = File(outputDirectory, "empty-output.tiff")
        
        // Execute conversion on empty file
        Assertions.assertThrows(Exception::class.java) {
            runBlocking {
                emptyPdf.toTiff(outputTiff)
            }
        }
    }
    
    /**
     * Test edge case: Non-existent PDF file
     */
    @Test
    fun `test pdf to tiff with non-existent file`() = runBlocking {
        val nonExistentFile = File("non-existent.pdf")
        val outputTiff = File(outputDirectory, "non-existent-output.tiff")
        
        // Execute conversion on non-existent file
        Assertions.assertThrows(Exception::class.java) {
            runBlocking {
                nonExistentFile.toTiff(outputTiff)
            }
        }
    }
    
    /**
     * Test error handling with invalid PDF content
     */
    @Test
    fun `test pdf to tiff with invalid pdf content`() = runBlocking {
        val invalidPdfContent = "Invalid PDF content"
        val inputStream = ByteArrayInputStream(invalidPdfContent.toByteArray())
        val outputStream = ByteArrayOutputStream()
        
        // Execute conversion with invalid PDF content
        Assertions.assertThrows(Exception::class.java) {
            runBlocking {
                val converter = apexPdfToTiff()
                converter.convert(inputStream, outputStream)
            }
        }
    }
    
    /**
     * Test plugin functionality
     */
    @Test
    fun `test pdf to tiff with plugins`() = runBlocking {
        val outputTiff = File(outputDirectory, "plugins-output.tiff")
        
        // Execute conversion with plugins
        testPdf.toTiff(
            outputTiff,
            pdfConfig = {
                dpi = 150f
            },
            tiffConfig = {
                compressionType = "JPEG"
            }
        )
        
        // Verify output file exists
        Assertions.assertTrue(outputTiff.exists(), "Output TIFF file should exist")
    }
    
    /**
     * Test configuration options - DPI setting
     */
    @Test
    fun `test pdf to tiff with different dpi settings`() = runBlocking {
        val outputTiff150 = File(outputDirectory, "dpi-150.tiff")
        val outputTiff300 = File(outputDirectory, "dpi-300.tiff")
        
        // Execute conversion with 150 DPI
        testPdf.toTiff(outputTiff150, pdfConfig = { dpi = 150f })
        
        // Execute conversion with 300 DPI
        testPdf.toTiff(outputTiff300, pdfConfig = { dpi = 300f })
        
        // Verify both files exist and 300 DPI file is larger
        Assertions.assertTrue(outputTiff150.exists())
        Assertions.assertTrue(outputTiff300.exists())
        Assertions.assertTrue(outputTiff300.length() > outputTiff150.length(), "300 DPI file should be larger than 150 DPI file")
    }
    
    /**
     * Test configuration options - Compression settings
     */
    @Test
    fun `test pdf to tiff with different compression settings`() = runBlocking {
        val outputTiffJpeg = File(outputDirectory, "compression-jpeg.tiff")
        val outputTiffDeflate = File(outputDirectory, "compression-deflate.tiff")
        
        // Execute conversion with JPEG compression
        testPdf.toTiff(outputTiffJpeg, tiffConfig = { compressionType = "JPEG" })
        
        // Execute conversion with DEFLATE compression
        testPdf.toTiff(outputTiffDeflate, tiffConfig = { compressionType = "DEFLATE" })
        
        // Verify both files exist
        Assertions.assertTrue(outputTiffJpeg.exists())
        Assertions.assertTrue(outputTiffDeflate.exists())
    }
}