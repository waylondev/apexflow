package dev.waylon.apexflow.conversion

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

/**
 * Benchmark for PDF to TIFF and TIFF to PDF conversion using JMH
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
class ConversionBenchmark {

    private val logger = LoggerFactory.getLogger(ConversionBenchmark::class.java)
    
    // Test PDF file path
    private val testPdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
    
    // PDF content as byte array for benchmarking
    private lateinit var pdfContent: ByteArray
    
    // TIFF content as byte array for benchmarking
    private lateinit var tiffContent: ByteArray
    
    @Setup
    fun setUp() {
        logger.info("Setting up benchmark...")
        
        // Load test PDF file
        pdfContent = testPdfFile.readBytes()
        logger.debug("Loaded test PDF file (size: {} bytes)", pdfContent.size)
        
        // Convert PDF to TIFF for TIFF to PDF benchmark
        val tiffOutputStream = ByteArrayOutputStream()
        runBlocking {
            pdfToTiff().convert(ByteArrayInputStream(pdfContent), tiffOutputStream)
        }
        tiffContent = tiffOutputStream.toByteArray()
        logger.debug("Generated test TIFF content (size: {} bytes)", tiffContent.size)
        
        logger.info("Benchmark setup completed")
    }
    
    @TearDown
    fun tearDown() {
        logger.info("Benchmark teardown completed")
    }
    
    /**
     * Benchmark for PDF to TIFF conversion
     */
    @Benchmark
    fun pdfToTiffConversion() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        pdfToTiff().convert(ByteArrayInputStream(pdfContent), outputStream)
    }
    
    /**
     * Benchmark for PDF to TIFF conversion with custom configuration
     */
    @Benchmark
    fun pdfToTiffConversionWithCustomConfig() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        pdfToTiff(
            pdfConfig = {
                dpi = 150f
                skipBlankPages = true
            },
            tiffConfig = {
                compressionType = "LZW"
                compressionQuality = 90f
            }
        ).convert(ByteArrayInputStream(pdfContent), outputStream)
    }
    
    /**
     * Benchmark for TIFF to PDF conversion
     */
    @Benchmark
    fun tiffToPdfConversion() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        tiffToPdf().convert(ByteArrayInputStream(tiffContent), outputStream)
    }
    
    /**
     * Benchmark for TIFF to PDF conversion with custom configuration
     */
    @Benchmark
    fun tiffToPdfConversionWithCustomConfig() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        tiffToPdf(
            pdfConfig = {
                jpegQuality = 90f
                compressContent = true
                pdfVersion = "1.7"
            }
        ).convert(ByteArrayInputStream(tiffContent), outputStream)
    }
}
