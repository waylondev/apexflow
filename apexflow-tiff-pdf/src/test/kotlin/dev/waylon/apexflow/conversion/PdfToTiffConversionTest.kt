package dev.waylon.apexflow.conversion

import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import dev.waylon.apexflow.core.dsl.transformOnDefault
import dev.waylon.apexflow.core.dsl.transformOnIO
import dev.waylon.apexflow.pdf.PdfImageReader
import dev.waylon.apexflow.tiff.TiffWriter
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Test case for converting a specific PDF file to TIFF
 *
 * This test converts the spring-boot-reference-3.3.0-977.pdf file from the build directory to TIFF
 */
class PdfToTiffConversionTest {

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
        val inputPdf = File("build/spring-boot-reference-406.pdf")
        val outputTiff = File("build/spring-boot-reference-406.tiff")

        // Check if input file exists
        assertTrue(inputPdf.exists(), "Input PDF file does not exist: ${inputPdf.absolutePath}")

        println("Converting PDF to TIFF...")
        println("Input: ${inputPdf.absolutePath}")
        println("Output: ${outputTiff.absolutePath}")
        println("Input size: ${inputPdf.length() / 1024} KB")

        // 核心设计：创建ApexFlow实例，体现"Everything is Flow"原则
        val pdfToTiffFlow = apexFlow<Pair<File, File>, Unit> {
            // Step 1: 打开文件流 (IO密集型，使用IO调度器)
            transformOnIO { (pdfFile, tiffFile) ->
                println("Step 1: Opening streams on ${Thread.currentThread().name}")
                val inputStream = Files.newInputStream(pdfFile.toPath())
                val outputStream = Files.newOutputStream(tiffFile.toPath())
                Triple(inputStream, outputStream, tiffFile) // 返回三元组，传递给下一步
            }

                // Step 2: 读取PDF获取图像流 (CPU密集型，使用Default调度器)
                .transformOnDefault { (inputStream, outputStream, tiffFile) ->
                    println("Step 2: Reading PDF on ${Thread.currentThread().name}")
                    val pdfReader = PdfImageReader(inputStream) {
                        dpi = 100f // 设置DPI
                    }
                    val imagesFlow = pdfReader.read() // 获取图像流
                    Triple(imagesFlow, outputStream, inputStream) // 返回三元组，传递给下一步
                }

                // Step 3: 创建TIFF写入器并写入 (IO密集型，使用IO调度器)
                .transformOnIO { (imagesFlow, outputStream, inputStream) ->
                    println("Step 3: Writing TIFF on ${Thread.currentThread().name}")
                    try {
                        val tiffWriter = TiffWriter(outputStream) {
                            compressionType = "JPEG" // 设置压缩类型
                            compressionQuality = 90f // 设置压缩质量
                        }
                        tiffWriter.write(imagesFlow) // 写入TIFF文件
                    } finally {
                        // 确保流被关闭
                        inputStream.close()
                        outputStream.close()
                    }
                }
        }

        // 执行流程：将输入文件对传递给流程
        // 使用execute方法，它是transform方法的便捷封装
        val result = pdfToTiffFlow.execute(inputPdf to outputTiff).toList()

        // Verify that output file was created
        assertTrue(outputTiff.exists(), "Output TIFF file was not created")
        assertTrue(outputTiff.length() > 0, "Output TIFF file is empty")

        println("Conversion completed successfully!")
        println("Output size: ${outputTiff.length() / 1024} KB")
    }
}