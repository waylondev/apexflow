package dev.waylon.apexflow.conversion

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * JMH Benchmark for PDF to TIFF and TIFF to PDF conversion
 *
 * Tests the performance of conversion with different configurations:
 * - Default settings
 * - Different DPI values
 * - Different compression types
 *
 * Run with: ./gradlew jmh
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, jvmArgsAppend = ["-Xmx4g", "-Xms2g"])
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
open class PdfTiffConversionBenchmark {

    private val testPdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")
    private val testTiffFile = File("src/test/resources/test-tiff-to-pdf-dsl.tiff")
    private val outputDir = File("build/benchmark")

    @Setup
    fun setup() {
        // Ensure output directory exists
        outputDir.mkdirs()
    }

    /**
     * Benchmark PDF to TIFF conversion with default settings
     */
    @Benchmark
    fun pdfToTiffDefault(blackhole: Blackhole) {
        val outputFile = File(outputDir, "benchmark-default.tiff")
        
        kotlinx.coroutines.runBlocking {
            pdfToTiff().convert(testPdfFile, outputFile)
        }
        
        blackhole.consume(outputFile.length())
        outputFile.delete()
    }

    /**
     * Benchmark PDF to TIFF conversion with low DPI (72)
     */
    @Benchmark
    fun pdfToTiffLowDpi(blackhole: Blackhole) {
        val outputFile = File(outputDir, "benchmark-low-dpi.tiff")
        
        kotlinx.coroutines.runBlocking {
            pdfToTiff(
                pdfConfig = { dpi = 72f }
            ).convert(testPdfFile, outputFile)
        }
        
        blackhole.consume(outputFile.length())
        outputFile.delete()
    }

    /**
     * Benchmark PDF to TIFF conversion with high DPI (300)
     */
    @Benchmark
    fun pdfToTiffHighDpi(blackhole: Blackhole) {
        val outputFile = File(outputDir, "benchmark-high-dpi.tiff")
        
        kotlinx.coroutines.runBlocking {
            pdfToTiff(
                pdfConfig = { dpi = 300f }
            ).convert(testPdfFile, outputFile)
        }
        
        blackhole.consume(outputFile.length())
        outputFile.delete()
    }

    /**
     * Benchmark PDF to TIFF conversion with LZW compression
     */
    @Benchmark
    fun pdfToTiffLzwCompression(blackhole: Blackhole) {
        val outputFile = File(outputDir, "benchmark-lzw.tiff")
        
        kotlinx.coroutines.runBlocking {
            pdfToTiff(
                tiffConfig = { compressionType = "LZW" }
            ).convert(testPdfFile, outputFile)
        }
        
        blackhole.consume(outputFile.length())
        outputFile.delete()
    }

    /**
     * Benchmark PDF to TIFF conversion with JPEG compression
     */
    @Benchmark
    fun pdfToTiffJpegCompression(blackhole: Blackhole) {
        val outputFile = File(outputDir, "benchmark-jpeg.tiff")
        
        kotlinx.coroutines.runBlocking {
            pdfToTiff(
                tiffConfig = { 
                    compressionType = "JPEG" 
                    compressionQuality = 90f 
                }
            ).convert(testPdfFile, outputFile)
        }
        
        blackhole.consume(outputFile.length())
        outputFile.delete()
    }

    /**
     * Benchmark TIFF to PDF conversion with default settings
     */
    @Benchmark
    fun tiffToPdfDefault(blackhole: Blackhole) {
        val outputFile = File(outputDir, "benchmark-tiff-to-pdf-default.pdf")
        
        kotlinx.coroutines.runBlocking {
            tiffToPdf().convert(testTiffFile, outputFile)
        }
        
        blackhole.consume(outputFile.length())
        outputFile.delete()
    }

    /**
     * Benchmark TIFF to PDF conversion with high JPEG quality
     */
    @Benchmark
    fun tiffToPdfHighQuality(blackhole: Blackhole) {
        val outputFile = File(outputDir, "benchmark-tiff-to-pdf-high-quality.pdf")
        
        kotlinx.coroutines.runBlocking {
            tiffToPdf(
                pdfConfig = { jpegQuality = 100f }
            ).convert(testTiffFile, outputFile)
        }
        
        blackhole.consume(outputFile.length())
        outputFile.delete()
    }
}
