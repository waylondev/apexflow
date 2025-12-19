package dev.waylon.apexflow.conversion


import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Simple benchmark test for PDF to TIFF and TIFF to PDF conversion
 * This test measures the time taken for conversion operations
 */
class SimpleConversionBenchmark {

    private val logger = LoggerFactory.getLogger(SimpleConversionBenchmark::class.java)

    // Test PDF file path
    private val testPdfFile = File("src/test/resources/test-pdf-to-tiff-dsl.pdf")

    @Test
    fun `benchmark pdf to tiff conversion`() = runBlocking {
        val pdfContent = testPdfFile.readBytes()
        logger.info("Starting PDF to TIFF conversion benchmark...")

        val iterations = 5
        var totalDuration = Duration.ZERO

        repeat(iterations) {
            val startTime = System.nanoTime()

            val outputStream = ByteArrayOutputStream()
            pdfToTiff().convert(ByteArrayInputStream(pdfContent), outputStream)

            val endTime = System.nanoTime()
            val duration = Duration.ofNanos(endTime - startTime)
            totalDuration += duration

            logger.debug("Iteration {}: {} ms", it + 1, duration.toMillis())
        }

        val averageDuration = totalDuration.dividedBy(iterations.toLong())
        logger.info("PDF to TIFF conversion benchmark completed")
        logger.info("Average time per iteration: {} ms", averageDuration.toMillis())
        logger.info("Total time for {} iterations: {} ms", iterations, totalDuration.toMillis())
    }

    @Test
    fun `benchmark tiff to pdf conversion`() = runBlocking {
        val pdfContent = testPdfFile.readBytes()

        // First convert PDF to TIFF for TIFF to PDF benchmark
        val tiffOutputStream = ByteArrayOutputStream()
        pdfToTiff().convert(ByteArrayInputStream(pdfContent), tiffOutputStream)
        val tiffContent = tiffOutputStream.toByteArray()

        logger.info("Starting TIFF to PDF conversion benchmark...")

        val iterations = 5
        var totalDuration = Duration.ZERO

        repeat(iterations) {
            val startTime = System.nanoTime()

            val outputStream = ByteArrayOutputStream()
            tiffToPdf().convert(ByteArrayInputStream(tiffContent), outputStream)

            val endTime = System.nanoTime()
            val duration = Duration.ofNanos(endTime - startTime)
            totalDuration += duration

            logger.debug("Iteration {}: {} ms", it + 1, duration.toMillis())
        }

        val averageDuration = totalDuration.dividedBy(iterations.toLong())
        logger.info("TIFF to PDF conversion benchmark completed")
        logger.info("Average time per iteration: {} ms", averageDuration.toMillis())
        logger.info("Total time for {} iterations: {} ms", iterations, totalDuration.toMillis())
    }
}