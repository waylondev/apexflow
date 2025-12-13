# ApexFlow - High Performance File Conversion Engine

ApexFlow is a modern, high-performance file conversion engine built on Kotlin coroutines and Flow API. It provides a flexible, extensible architecture for building efficient file conversion workflows.

## Project Structure

```
├── apexflow-core/          # Core workflow engine and interfaces
├── apexflow-pdf-pdfbox/   # PDF format support using PDFBox
├── apexflow-tiff-twelvemonkeys/ # TIFF format support using TwelveMonkeys
└── apexflow-example/       # Example usage and demonstrations
```

## High Performance Design

### 1. Kotlin Coroutine Flow Architecture
- **Asynchronous Processing**: Leverages coroutines and Flow API for non-blocking execution
- **Parallel Pipeline**: Three-stage parallel processing:
  - **Reading**: Uses `Dispatchers.IO` for IO-intensive operations
  - **Processing**: Uses `Dispatchers.Default` for CPU-intensive tasks
  - **Writing**: Uses `Dispatchers.IO` for IO-intensive operations
- **Built-in Backpressure**: Flow API automatically handles backpressure, optimizing memory usage

### 2. Optimized Buffer Management
- **Configurable Buffers**: Adjustable buffer sizes for read and process stages
- **Balanced Throughput**: Fine-tuning of throughput between stages
- **Memory Efficient**: Prevents memory bloat while maintaining high performance

### 3. Low Overhead Design
- **Lazy Initialization**: Efficient resource usage with `by lazy` properties
- **Minimal State**: No complex status tracking for optimal performance
- **Fast Path Execution**: Focus on critical path with minimal overhead
- **Conversion-Optimized**: Designed specifically for high-throughput file conversion

### 4. Efficient Compression
- **LZW Compression**: TIFF output with efficient LZW compression
- **JPEG Compression**: PDF output with configurable JPEG compression
- **DPI Control**: Adjustable resolution for optimal quality/size balance

## Extensibility Design

### 1. SOLID-Based Architecture

ApexFlow's extensibility is built on solid SOLID principles:

```kotlin
// Core interfaces following Interface Segregation Principle
interface WorkflowReader<I>     // Single responsibility: read input data
interface WorkflowProcessor<I, O>  // Single responsibility: process/convert data
interface WorkflowWriter<O>    // Single responsibility: write output data
interface WorkflowEngine       // Single responsibility: coordinate workflow execution
```

### 2. Modular Design
- **Separation of Concerns**: Clear division between core engine and format-specific implementations
- **Dependency Inversion**: Core engine depends on abstractions, not concrete implementations
- **Easy to Test**: Interfaces facilitate mocking and testing
- **Open/Closed Principle**: Extensible without modifying core code

### 3. DSL Builder Pattern
```kotlin
// Fluent DSL demonstrating Kotlin best practices
val engine = apexFlowWorkflow {
    reader(TiffReader(inputPath = inputPath))
    processor(WorkflowProcessor.identity())
    writer(PdfWriter(outputPath))
    configure {
        readBufferSize = 500
        processBufferSize = 500
    }
}
```

### 4. Format Support
- **PDF Support**: Implemented in `apexflow-pdf-pdfbox` module
- **TIFF Support**: Implemented in `apexflow-tiff-twelvemonkeys` module
- **Easy to Add New Formats**: Simply implement the core interfaces (Liskov Substitution Principle)

### 5. Configuration Flexibility
```kotlin
// Immutable data class following Kotlin best practices
data class WorkflowConfig(
    val readBufferSize: Int = 100,
    val processBufferSize: Int = 100,
    val ioBufferSize: Int = 4 * 8192
    // Dispatchers, error handling, and other configuration
)
```

## Core Components

### Workflow Engine
- **ApexFlowWorkflowEngine**: High-performance implementation based on Kotlin Flow, focusing solely on workflow coordination
- **WorkflowConfig**: Immutable data class for comprehensive configuration, supporting buffer management
- **WorkflowStatus**: Simple status tracking with minimal overhead

### Readers
- **FileWorkflowReader**: Abstract base class for file-based readers
- **PdfReader**: PDF format reader with configurable DPI and render quality
- **TiffReader**: TIFF format reader supporting multi-page files

### Processors
- **BatchWorkflowProcessor**: Batch processing implementation
- **Identity Processor**: Lightweight default implementation for pass-through scenarios
- **Custom Processors**: Easy to implement for specific conversion needs

### Writers
- **FileWorkflowWriter**: Abstract base class for file-based writers
- **PdfWriter**: PDF format writer with JPEG compression
- **TiffWriter**: TIFF format writer with LZW compression

## Usage Examples

### Basic TIFF to PDF Conversion
```kotlin
val engine = apexFlowWorkflow {
    reader(TiffReader(inputPath = "input.tif"))
    processor(WorkflowProcessor.identity())
    writer(PdfWriter("output.pdf"))
}

runBlocking {
    engine.startAsync()
}
```

### PDF to TIFF Conversion
```kotlin
import dev.waylon.apexflow.pdf.RenderQuality

val engine = apexFlowWorkflow {
    reader(PdfReader(
        inputPath = "input.pdf",
        dpi = 50f,
        renderQuality = RenderQuality.BALANCED
    ))
    processor(WorkflowProcessor.identity())
    writer(TiffWriter("output.tif"))
}

runBlocking {
    engine.startAsync()
}
```

### With Buffer Configuration
```kotlin
val engine = apexFlowWorkflow {
    reader(TiffReader(inputPath = "input.tif"))
    processor(WorkflowProcessor.identity())
    writer(PdfWriter("output.pdf"))
    configure {
        readBufferSize = 500  // Adjust based on your needs
        processBufferSize = 500  // Adjust based on your needs
    }
}

runBlocking {
    engine.startAsync()
}
```

## Performance Characteristics

- **Throughput**: Optimized for high pages per second conversion
- **Memory Usage**: Stream-based processing minimizes memory footprint
- **CPU Utilization**: Efficiently utilizes multi-core systems
- **Scalability**: Handles large files with millions of pages

## Extending ApexFlow

### Adding a New File Format
1. Implement `WorkflowReader<I>` for reading the new format
2. Implement `WorkflowWriter<O>` for writing the new format
3. Optionally implement `WorkflowProcessor<I, O>` for format-specific processing
4. Use the DSL to create workflows with your new components

### Custom Processing
1. Implement `WorkflowProcessor<I, O>` with your custom logic
2. Integrate into workflows using the DSL
3. Leverage parallel processing automatically

## Technology Stack

- **Kotlin**: Modern programming language
- **Kotlin Coroutines**: Asynchronous programming
- **Kotlin Flow**: Reactive stream processing
- **PDFBox**: PDF format support
- **TwelveMonkeys**: TIFF format support
- **SLF4J**: Logging abstraction

## Build and Run

### Building the Project
```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :apexflow-core:build
```

### Example Types

1. **Basic Conversion**: Simple TIFF to PDF conversion demonstrating the core API
2. **Configured Conversion**: Demonstrates buffer configuration and performance monitoring
3. **Advanced Conversion**: Shows custom processing and watermarking capabilities
4. **PDF to TIFF Conversion**: Demonstrates PDF to TIFF conversion with file size optimization

### Creating Your Own Workflows

To create custom workflows, follow these steps:

1. **Implement Interfaces**: Create implementations of `WorkflowReader`, `WorkflowProcessor`, and `WorkflowWriter` as needed
2. **Build Workflow**: Use the DSL to construct your workflow
3. **Configure**: Set appropriate buffer sizes and performance settings
4. **Execute**: Run the workflow using `startAsync()` or `start()`

## Design Principles

### Core Design Philosophy

ApexFlow is built on a foundation of modern software engineering principles:

1. **High Performance First**: Every design decision prioritizes throughput and minimal memory footprint
2. **Simplicity**: Clean, intuitive API and architecture that's easy to understand and use
3. **Extensibility**: Simple interface-based design for easy addition of new formats and features
4. **Reliability**: Robust error handling and predictable behavior
5. **Modern**: Leverages Kotlin's coroutines and Flow API for efficient asynchronous processing

### SOLID Architecture

ApexFlow follows SOLID principles for maintainable, extensible code:

- **Single Responsibility Principle**: Each component has a clear, focused purpose
- **Open/Closed Principle**: Extensible without modifying core code
- **Liskov Substitution Principle**: Interface implementations are interchangeable
- **Interface Segregation Principle**: Small, focused interfaces
- **Dependency Inversion Principle**: Core depends on abstractions, not concrete implementations

### Kotlin Best Practices

The codebase embraces modern Kotlin features:

- **Coroutines & Flow API**: Asynchronous, non-blocking processing with backpressure
- **Null Safety**: Complete null safety guarantees
- **Immutability**: Immutable data classes by default
- **DSL Builder Pattern**: Fluent, type-safe workflow construction
- **Resource Management**: Safe handling with `use` blocks
- **Lazy Initialization**: Efficient resource usage

## Conclusion

ApexFlow is a **high-performance, SOLID-designed file conversion engine** built for modern Kotlin applications. Its architecture combines:

- **Robust SOLID Principles**: Ensuring maintainability, extensibility, and testability
- **Modern Kotlin Features**: Leveraging coroutines, Flow API, and other language enhancements
- **Optimized Performance**: Focused on high-throughput file conversion workloads
- **Modular Design**: Easy to extend with new formats and processing capabilities
- **Developer-Friendly API**: Fluent DSL for intuitive workflow construction

### Key Strengths

1. **Performance**: Optimized for high pages-per-second conversion with minimal memory footprint
2. **Extensibility**: Simple interface implementation for new formats and features
3. **Reliability**: Robust error handling and predictable behavior
4. **Maintainability**: Clean, modular architecture following SOLID principles
5. **Modern**: Built with the latest Kotlin best practices

### Use Cases

ApexFlow is ideal for:
- **High-volume document conversion**
- **Batch processing of large files**
- **Custom conversion workflows**
- **Performance-critical applications**
- **Extensible conversion platforms**

Whether you need to convert TIFF to PDF, PDF to TIFF, or build custom conversion pipelines, ApexFlow provides a solid foundation with excellent performance characteristics and extensibility.