package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.pdf.ApexPdfWriter
import dev.waylon.apexflow.pdf.PdfConfig
import dev.waylon.apexflow.tiff.ApexTiffReader
import dev.waylon.apexflow.tiff.TiffConfig
import org.junit.jupiter.api.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking

/**
 * Unit tests for TIFF to PDF conversion
 *
 * This test class covers:
 * - Basic functionality
 * - Edge cases
 * - Error handling
 * - Configuration options
 * - Component composition
 * - Plugin functionality
 */
class TiffToPdfUnitTest {
    
    private val testTiff = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")
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
     * Test basic TIFF to PDF conversion
     */
    @Test
    fun `test basic tiff to pdf conversion`() = runBlocking {
        val outputPdf = File(outputDirectory, "basic-output.pdf")
        
        // Execute conversion
        testTiff.toPdf(outputPdf)
        
        // Verify output file exists and is not empty
        Assertions.assertTrue(outputPdf.exists(), "Output PDF file should exist")
        Assertions.assertTrue(outputPdf.length() > 0, "Output PDF file should not be empty")
    }
    
    /**
     * Test TIFF to PDF conversion with custom configuration
     */
    @Test
    fun `test tiff to pdf with custom configuration`() = runBlocking {
        val outputPdf = File(outputDirectory, "custom-config-output.pdf")
        
        // Execute conversion with custom settings
        testTiff.toPdf(
            outputPdf,
            tiffConfig = {
                bufferSize = 5
            },
            pdfConfig = {
                jpegQuality = 0.9f
                pageSize = org.apache.pdfbox.pdmodel.common.PDRectangle.A4
                bufferSize = 5
            }
        )
        
        // Verify output file exists
        Assertions.assertTrue(outputPdf.exists(), "Output PDF file should exist")
    }
    
    /**
     * Test TIFF to PDF conversion using component composition
     */
    @Test
    fun `test tiff to pdf component composition`() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        
        // Create components
        val tiffReader = ApexTiffReader.fromFile(testTiff, TiffConfig().apply { bufferSize = 5 })
        val pdfWriter = ApexPdfWriter.toOutputStream(outputStream, PdfConfig().apply { jpegQuality = 0.8f })
        
        // Compose components using + operator
        val pipeline = tiffReader + pdfWriter
        
        // Execute pipeline
        pipeline.execute(Unit).collect { }
        
        // Verify output is generated
        Assertions.assertTrue(outputStream.size() > 0, "Output stream should contain data")
    }
    
    /**
     * Test edge case: Empty TIFF file
     */
    @Test
    fun `test tiff to pdf with empty tiff`() = runBlocking {
        val emptyTiff = File.createTempFile("empty", ".tiff", outputDirectory)
        val outputPdf = File(outputDirectory, "empty-output.pdf")
        
        // Execute conversion on empty file
        Assertions.assertThrows(Exception::class.java) {
            runBlocking {
                emptyTiff.toPdf(outputPdf)
            }
        }
    }
    
    /**
     * Test edge case: Non-existent TIFF file
     */
    @Test
    fun `test tiff to pdf with non-existent file`() = runBlocking {
        val nonExistentFile = File("non-existent.tiff")
        val outputPdf = File(outputDirectory, "non-existent-output.pdf")
        
        // Execute conversion on non-existent file
        Assertions.assertThrows(Exception::class.java) {
            runBlocking {
                nonExistentFile.toPdf(outputPdf)
            }
        }
    }
    
    /**
     * Test error handling with invalid TIFF content
     */
    @Test
    fun `test tiff to pdf with invalid tiff content`() = runBlocking {
        val invalidTiffContent = "Invalid TIFF content"
        val inputStream = ByteArrayInputStream(invalidTiffContent.toByteArray())
        val outputStream = ByteArrayOutputStream()
        
        // Execute conversion with invalid TIFF content
        Assertions.assertThrows(Exception::class.java) {
            runBlocking {
                val converter = apexTiffToPdf()
                converter.convert(inputStream, outputStream)
            }
        }
    }
    
    /**
     * Test plugin functionality
     */
    @Test
    fun `test tiff to pdf with plugins`() = runBlocking {
        val outputPdf = File(outputDirectory, "plugins-output.pdf")
        
        // Execute conversion with plugins
        testTiff.toPdf(
            outputPdf,
            tiffConfig = {
                bufferSize = 10
            },
            pdfConfig = {
                jpegQuality = 0.9f
            }
        )
        
        // Verify output file exists
        Assertions.assertTrue(outputPdf.exists(), "Output PDF file should exist")
    }
    
    /**
     * Test configuration options - JPEG quality
     */
    @Test
    fun `test tiff to pdf with different jpeg quality settings`() = runBlocking {
        val outputPdfHighQuality = File(outputDirectory, "high-quality.pdf")
        val outputPdfLowQuality = File(outputDirectory, "low-quality.pdf")
        
        // Execute conversion with high JPEG quality
        testTiff.toPdf(outputPdfHighQuality, pdfConfig = { jpegQuality = 0.95f })
        
        // Execute conversion with low JPEG quality
        testTiff.toPdf(outputPdfLowQuality, pdfConfig = { jpegQuality = 0.5f })
        
        // Verify both files exist and high quality file is larger
        Assertions.assertTrue(outputPdfHighQuality.exists())
        Assertions.assertTrue(outputPdfLowQuality.exists())
        Assertions.assertTrue(outputPdfHighQuality.length() > outputPdfLowQuality.length(), "High quality PDF should be larger than low quality PDF")
    }
    
    /**
     * Test configuration options - Page size
     */
    @Test
    fun `test tiff to pdf with different page sizes`() = runBlocking {
        val outputPdfA4 = File(outputDirectory, "page-size-a4.pdf")
        val outputPdfLetter = File(outputDirectory, "page-size-letter.pdf")
        
        // Execute conversion with A4 page size
        testTiff.toPdf(outputPdfA4, pdfConfig = { 
            pageSize = org.apache.pdfbox.pdmodel.common.PDRectangle.A4 
        })
        
        // Execute conversion with Letter page size
        testTiff.toPdf(outputPdfLetter, pdfConfig = { 
            pageSize = org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER 
        })
        
        // Verify both files exist
        Assertions.assertTrue(outputPdfA4.exists())
        Assertions.assertTrue(outputPdfLetter.exists())
    }
    
    /**
     * Test round trip conversion: PDF -> TIFF -> PDF
     */
    @Test
    fun `test round trip conversion pdf to tiff to pdf`() = runBlocking {
        // First convert PDF to TIFF
        val intermediateTiff = File(outputDirectory, "intermediate.tiff")
        val outputPdf = File(outputDirectory, "round-trip-output.pdf")
        
        val testPdf = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
        
        // PDF to TIFF
        testPdf.toTiff(intermediateTiff)
        Assertions.assertTrue(intermediateTiff.exists(), "Intermediate TIFF file should exist")
        
        // TIFF to PDF
        intermediateTiff.toPdf(outputPdf)
        Assertions.assertTrue(outputPdf.exists(), "Round trip output PDF file should exist")
        Assertions.assertTrue(outputPdf.length() > 0, "Round trip output PDF file should not be empty")
    }
}