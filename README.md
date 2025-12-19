# ApexFlow

A modern, high-performance, and highly extensible framework for document processing and conversion, built with Kotlin and cutting-edge technologies.

## 🚀 Key Features

### Modern Architecture
- **Kotlin-First Design**: Leverages Kotlin's concise syntax, null safety, and functional programming capabilities
- **Coroutine-Powered**: Built on Kotlin Coroutines for efficient asynchronous processing
- **Flow-Based API**: Uses Flow API for reactive, backpressure-aware stream processing
- **DSL-Friendly**: Fluent Domain-Specific Languages for intuitive usage

### High Performance
- **Stream-Based Processing**: Core design emphasizes stream processing to minimize memory footprint
- **Efficient I/O Handling**: Optimized for large file processing with minimal buffering
- **Parallel Execution**: Smart parallelization of CPU-intensive tasks
- **Low Overhead**: Minimal framework overhead for maximum throughput

### Highly Extensible
- **Modular Architecture**: Clean separation of concerns with pluggable components
- **Configuration-Driven**: Flexible configuration system for easy customization
- **Extension Functions**: Rich set of extension functions for enhanced usability
- **Customizable Processors**: Easy to add custom conversion logic and processors

### Cutting-Edge Technology Stack
- **Kotlin 1.9+**: Latest Kotlin features including sealed classes, data classes, and type aliases
- **Kotlin Coroutines**: Asynchronous programming with structured concurrency
- **Flow API**: Reactive streams with backpressure support
- **PDFBox**: Industry-standard PDF processing library
- **TwelveMonkeys ImageIO**: High-performance image processing with TIFF support

## 📋 Core Functionalities

### Document Conversion
- **PDF to TIFF**: High-quality PDF to TIFF conversion with customizable DPI and compression
- **TIFF to PDF**: Efficient TIFF to PDF conversion with support for multi-page TIFFs
- **Bidirectional Support**: Seamless conversion in both directions

### Input/Output Flexibility
- **File Support**: Direct file-to-file conversion
- **Stream Support**: InputStream/OutputStream for in-memory processing
- **Path Support**: String paths for convenient usage
- **Flow Support**: Flow-based processing for reactive pipelines

### Configuration Options
- **PDF Configuration**: DPI, page filtering, blank page skipping
- **TIFF Configuration**: Compression type, quality, color mode
- **PDF Writing**: JPEG quality, compression, PDF version
- **Runtime Customization**: Configure processing parameters at runtime

## 🏗️ Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────┐
│                    ApexFlow Framework                   │
├───────────────────┬───────────────────┬─────────────────┤
│   Document DSLs   │   I/O Processors  │   Conversion    │
│                   │                   │   Pipelines     │
├───────────────────┼───────────────────┼─────────────────┤
│ - pdfToTiff()     │ - PdfImageReader  │ - ApexFlow Core │
│ - tiffToPdf()     │ - TiffWriter      │ - TransformOnIO │
│ - Extension Funcs │ - TiffReader      │ - WithTiming    │
│                   │ - PdfImageWriter  │                 │
└───────────────────┴───────────────────┴─────────────────┘
```

### Processing Pipeline

1. **Input Handling**: Read from File, InputStream, or String path
2. **Conversion Stage**: Transform data using specialized processors
3. **Output Handling**: Write to File, OutputStream, or String path
4. **Monitoring**: Built-in timing and logging for performance analysis

## 💡 Usage Examples

### Basic PDF to TIFF Conversion

```kotlin
import dev.waylon.apexflow.conversion.pdfToTiff
import java.io.File
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val inputFile = File("input.pdf")
    val outputFile = File("output.tiff")
    
    // Simple conversion with default settings
    pdfToTiff().convert(inputFile, outputFile)
}
```

### Custom Configuration

```kotlin
pdfToTiff(
    pdfConfig = { 
        dpi = 300f
        skipBlankPages = true
    },
    tiffConfig = { 
        compressionType = "LZW"
        compressionQuality = 100f
    }
).convert(inputFile, outputFile)
```

### Extension Functions

```kotlin
// File extension functions
File("input.pdf").toTiff(File("output.tiff"))
File("input.tiff").toPdf(File("output.pdf"))

// InputStream/OutputStream
inputStream.toTiff(outputStream)
inputStream.toPdf(outputStream)
```

### String Paths

```kotlin
pdfToTiff().convert("input.pdf", "output.tiff")
tiffToPdf().convert("input.tiff", "output.pdf")
```

## 🎯 Design Principles

### Simplicity First
- Intuitive API design with minimal boilerplate
- Clear, concise documentation
- Easy to learn, hard to misuse

### Type Safety
- No `Any` types in public APIs
- Compile-time type checking
- Strongly typed configurations

### Robust Error Handling
- Comprehensive exception handling
- Clear error messages
- Graceful degradation

### Performance-Oriented
- Stream-based processing by default
- Efficient memory usage
- Optimized for large files

## 🔧 Getting Started

### Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.waylon:apexflow:1.0.0")
}
```

### Prerequisites

- Java 11+ (required for PDFBox)
- Kotlin 1.9+

## 📊 Performance Characteristics

- **Memory Usage**: Constant memory footprint for large files
- **Throughput**: Optimized for high-speed conversion
- **Scalability**: Handles files of any size
- **Concurrent Processing**: Safe for concurrent usage

## 🎨 Use Cases

### Business Applications
- **Document Archiving**: Convert scanned documents to searchable PDFs
- **Print Workflows**: Prepare documents for printing with optimal formatting
- **Content Management**: Process and transform documents in CMS systems
- **E-commerce**: Generate product catalogs and brochures

### Enterprise Integration
- **Workflow Automation**: Integrate with workflow engines for automated document processing
- **Microservices**: Deploy as lightweight microservices for document conversion
- **Batch Processing**: Process large volumes of documents efficiently

## 🔮 Future Roadmap

- [ ] Support for more document formats (PNG, JPEG, etc.)
- [ ] OCR integration for text extraction
- [ ] Cloud-native deployment options
- [ ] WebSocket support for real-time processing
- [ ] GUI tools for interactive configuration

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

## 📄 License

Apache License 2.0

## 📞 Support

For questions, issues, or feedback, please create an issue in the GitHub repository.

---

Built with ❤️ using modern Kotlin technologies

ApexFlow - The future of document processing
