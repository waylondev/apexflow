# ApexFlow - High Performance File Conversion Engine

ApexFlow is a modern, high-performance file conversion engine built on Kotlin coroutines and Flow API. It provides a flexible, extensible architecture for building efficient file conversion workflows.

## Quick Start

### Simplified TIFF to PDF Conversion

```kotlin
// Add this import for the concise API
import dev.waylon.apexflow.dsl.tiffToPdf

// Simplest usage: just input and output paths
val engine = tiffToPdf("input.tif", "output.pdf")
runBlocking { engine.startAsync() }
```

### Simplified PDF to TIFF Conversion

```kotlin
// Add this import for the concise API
import dev.waylon.apexflow.dsl.pdfToTiff

// Simplest usage: just input and output paths
val engine = pdfToTiff("input.pdf", "output.tif")
runBlocking { engine.startAsync() }
```

### Traditional DSL Usage

```kotlin
// No extra imports needed for core DSL
val engine = apexFlowWorkflow {
    reader(TiffReader(inputPath = "input.tif"))
    processor(WorkflowProcessor.identity())
    writer(PdfImageWriter("output.pdf"))
    configure {
        readBufferSize = 200
        processBufferSize = 200
    }
}

runBlocking { engine.startAsync() }
```

### Module Structure

```
├── apexflow-core/                    # Core workflow engine (format-agnostic)
├── apexflow-pdf-pdfbox/             # PDF format support
├── apexflow-tiff-twelvemonkeys/     # TIFF format support
├── apexflow-dsl-extensions/         # Concise DSL extensions for common cases
└── apexflow-example/                # Example usage
```

## Key Features

### High Performance
- **Asynchronous Processing**: Leverages coroutines and Flow API for non-blocking execution
- **Parallel Pipeline**: Three-stage parallel processing with optimized dispatchers
- **Built-in Backpressure**: Flow API automatically handles backpressure, optimizing memory usage
- **Optimized Buffer Management**: Configurable buffer sizes for balanced throughput
- **Low Overhead Design**: Focus on critical path with minimal overhead

### Developer Friendly
- **Fluent DSL**: Type-safe workflow construction with intuitive syntax
- **Unified Interface**: Consistent API across all conversion types
- **Comprehensive Error Handling**: Built-in exception management
- **Immutable Configuration**: Thread-safe workflow configuration

### Extensible Architecture
- **SOLID Principles**: Clean, maintainable code following best practices
- **Plug-and-Play Components**: Easy to extend with custom readers, processors, and writers
- **Format Support**: Built-in PDF and TIFF support, extensible to other formats

## Technology Stack

- **Kotlin**: Modern programming language
- **Kotlin Coroutines**: Asynchronous programming
- **Kotlin Flow**: Reactive stream processing
- **PDFBox**: PDF format support
- **TwelveMonkeys**: TIFF format support
- **SLF4J**: Logging abstraction

## Project Structure

```
├── apexflow-core/          # Core workflow engine and interfaces
├── apexflow-pdf-pdfbox/   # PDF format support using PDFBox
├── apexflow-tiff-twelvemonkeys/ # TIFF format support using TwelveMonkeys
└── apexflow-example/       # Example usage and demonstrations
```

## Performance Test Results

| Test Scenario | Result |
|---------------|--------|
| 10,000 item processing | ✅ Successfully completed |
| Backpressure handling | ✅ Automatically managed |
| Buffer size testing | ✅ Works correctly with all sizes |
| Edge case handling | ✅ Passes all edge cases |
| Exception handling | ✅ Correctly handles all exceptions |

## Build and Run

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :apexflow-core:build
```

## Conclusion

ApexFlow is a **high-performance file conversion engine** built for modern Kotlin applications, offering:

- **Simple API**: Intuitive DSL for easy workflow construction
- **High Performance**: Asynchronous, parallel processing with built-in backpressure
- **Robust Design**: Reliable error handling and edge case management
- **Extensible Architecture**: Easy to add new formats and custom processing
- **Modern Technology**: Built with Kotlin coroutines and Flow API

ApexFlow simplifies file conversion workflows while delivering excellent performance, making it ideal for high-volume document processing and custom conversion pipelines.