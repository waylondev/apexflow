package dev.waylon.apexflow.pdf

import dev.waylon.apexflow.core.util.createLogger
import dev.waylon.apexflow.image.ApexImageReader
import dev.waylon.apexflow.image.ImageConstants
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.rendering.PDFRenderer

/**
 * PDF image reader configuration
 */
class PdfImageReaderConfig {
    /**
     * DPI (dots per inch) for rendering PDF pages
     * Higher values result in better quality but larger file sizes
     */
    var dpi: Float = ImageConstants.DEFAULT_DPI

    /**
     * Page numbers to render (0-based index)
     * If empty, all pages will be rendered
     */
    var pageNumbers: List<Int> = emptyList()

    /**
     * Whether to skip blank pages during rendering
     */
    var skipBlankPages: Boolean = false

    /**
     * Image type to use for rendering
     */
    var imageType: ImageType = ImageType.RGB

    /**
     * Whether to read pages in parallel
     */
    var parallelReading: Boolean = false

    /**
     * Maximum number of pages to read in parallel
     */
    var parallelism: Int = Runtime.getRuntime().availableProcessors()

    /**
     * Image type sealed class for PDF rendering
     * Provides type-safe extensibility for different image types
     */
    sealed class ImageType {
        /** RGB color space with 8 bits per component */
        object RGB : ImageType()

        /** Grayscale color space with 8 bits per component */
        object GRAY : ImageType()

        /** Binary (black and white) color space */
        object BINARY : ImageType()

        /** Custom image type with specified bits per component */
        data class Custom(val bitsPerComponent: Int) : ImageType()
    }
}

/**
 * PDF image reader implementation using PDFBox library
 *
 * Supports reading from InputStream with streaming behavior
 */
class PdfImageReader @JvmOverloads constructor(
    private val inputStream: InputStream,
    private val config: PdfImageReaderConfig = PdfImageReaderConfig()
) : ApexImageReader {
    /**
     * Convenience constructor: File + configuration
     */
    @JvmOverloads
    constructor(
        file: File,
        config: PdfImageReaderConfig = PdfImageReaderConfig()
    ) : this(file.inputStream(), config)

    private val logger = createLogger<PdfImageReader>()

    /**
     * Read PDF pages and return a Flow of BufferedImage with streaming behavior
     *
     * Each page is rendered as a separate BufferedImage with the configured DPI
     * Supports parallel rendering when parallelReading is enabled while maintaining streaming characteristics
     *
     * @return Flow<BufferedImage> Flow of rendered pages in original order
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun read(): Flow<BufferedImage> = flow {
        logger.info("Starting PDF reading process with DPI: {}, parallelReading: {}, parallelism: {}", 
                   config.dpi, config.parallelReading, config.parallelism)

        // PDFBox 3.0.1 supports ByteArray, so we'll convert InputStream to ByteArray
        Loader.loadPDF(RandomAccessReadBuffer(inputStream)).use { document ->
            logger.debug("Loaded PDF document successfully")

            val renderer = PDFRenderer(document)
            val pageCount = document.numberOfPages
            logger.info("Found {} pages in PDF document", pageCount)

            val pagesToRender = if (config.pageNumbers.isEmpty()) {
                0 until pageCount
            } else {
                config.pageNumbers.filter { it in 0 until pageCount }
            }

            // 使用Kotlin Flow的flatMapMerge实现并行读取，保持顺序
            // 🌟 核心：flatMapMerge是非阻塞的，完全符合Flow特性
            pagesToRender.asFlow()
                .flatMapMerge(config.parallelism) { pageIndex ->
                    // 为每个页面创建一个独立的Flow，在IO线程执行
                    renderPage(renderer, pageIndex, pageCount)
                }
                .collect { image ->
                    emit(image)
                    image.flush()
                    logger.debug("Successfully rendered PDF page")
                }
        }

        logger.info("Completed PDF reading process successfully")
    }
    
    /**
     * Render a single PDF page as a Flow<BufferedImage>
     * 
     * @param renderer PDFRenderer instance
     * @param pageIndex Index of the page to render
     * @param pageCount Total number of pages in the document
     * @return Flow emitting the rendered image
     */
    private fun renderPage(renderer: PDFRenderer, pageIndex: Int, pageCount: Int): Flow<BufferedImage> = flow {
        logger.debug("Rendering PDF page {}/{} in thread {}", 
                    pageIndex + 1, pageCount, Thread.currentThread().name)
        
        // 渲染页面 - 这是一个阻塞操作，但在IO线程池中执行
        val renderedImage = renderer.renderImageWithDPI(pageIndex, config.dpi)
        
        // 发射渲染结果
        emit(renderedImage)
    }.flowOn(Dispatchers.IO)  // 在IO线程池中执行，避免阻塞主线程

}

/**
 * Extension function: Convert InputStream to PdfImageReader with lambda configuration
 *
 * @param config Lambda function to configure PDF reader settings
 * @return PdfImageReader instance with specified configuration
 */
fun InputStream.toPdfImageReader(config: PdfImageReaderConfig.() -> Unit = {}): PdfImageReader {
    return PdfImageReader(this, PdfImageReaderConfig().apply(config))
}

/**
 * Extension function: Convert File to PdfImageReader with lambda configuration
 *
 * @param config Lambda function to configure PDF reader settings
 * @return PdfImageReader instance with specified configuration
 */
fun File.toPdfImageReader(config: PdfImageReaderConfig.() -> Unit = {}): PdfImageReader {
    return PdfImageReader(this, PdfImageReaderConfig().apply(config))
}