# ApexFlow

A modern, high-performance framework for document processing, conversion, and workflow orchestration, built with Kotlin and cutting-edge technologies.

## 🎯 Core Design Philosophy

At the heart of ApexFlow lies a simple yet powerful principle: **"Everything is Flow"**.

### Key Design Principles
- **Component-Based**: Build complex workflows from simple, reusable components
- **Declarative**: Focus on "what to do" rather than "how to do it"
- **Type-Safe**: Complete compile-time type checking
- **Asynchronous**: Built on Kotlin Coroutines for efficient async processing
- **Reactive**: Leverage Kotlin Flow for backpressure-aware stream processing

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
│ - tiffToPdf()     │ - TiffWriter      │ - Component     │
│                   │ - TiffReader      │   Composition   │
│                   │ - PdfImageWriter  │                 │
└───────────────────┴───────────────────┴─────────────────┘
```

### Architecture Highlights
- **Kotlin-First Design**: Leverages Kotlin's modern features
- **Modular Design**: Clean separation of concerns
- **Simple Composition**: Use `+` operator to combine components
- **Low Overhead**: Minimal framework overhead

## 🚀 Key Features

### ApexFlow Core - Workflow Orchestration
- **Powerful Component Composition**: Build complex workflows by combining simple components using the `+` operator
  - Example: `validation + dbQuery + apiCall + mergeResults + response`
  - Benefits: Reusability, maintainability, and clear flow visualization
- **Type-Safe DSL**: Intuitive DSL with complete compile-time type checking
- **Parallel Processing**: Easy implementation of parallel execution patterns
- **Testable Components**: Each component can be tested independently for reliability

### Component Composition - Key Advantage
ApexFlow's component composition enables:
- **Modular Development**: Build workflows from small, focused components
- **Reusability**: Components can be reused across multiple workflows
- **Maintainability**: Easy to modify or extend workflows by adding/removing components
- **Readability**: Clear, declarative syntax that shows the workflow structure at a glance
- **Type Safety**: Compile-time checks ensure component compatibility

### Document Processing
- **PDF ↔ TIFF Conversion**: High-quality bidirectional conversion
- **Customizable**: Configure DPI, compression, and more
- **Stream-Based**: Optimized for large files with minimal memory usage
- **Extensible**: Easy to add custom conversion logic

### Modern Technology Stack
- **Kotlin 1.9+**: Latest Kotlin features
- **Kotlin Coroutines**: Asynchronous programming
- **Flow API**: Reactive streams
- **PDFBox**: Industry-standard PDF processing
- **TwelveMonkeys ImageIO**: High-performance image processing

## 💡 Quick Start

### Basic Usage
```kotlin
import dev.waylon.apexflow.conversion.pdfToTiff
import java.io.File
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val inputFile = File("input.pdf")
    val outputFile = File("output.tiff")
    
    // Simple PDF to TIFF conversion
    pdfToTiff().convert(inputFile, outputFile)
}
```

### Workflow Example - Reusable Components
```kotlin
// Step 1: Define reusable components
val validation = apexFlow { map(::validatedRequest) }           // Reusable validation component
val dbQuery = apexFlow { map { queryDb(it) } }                 // Reusable DB query component
val apiCall = apexFlow { map { callThirdPartyApi(it) } }       // Reusable API call component
val mergeResults = apexFlow { map { (db, api) -> MergedResult(db.id, db.dbData, api.apiData) } } // Reusable merge component
val successResponse = apexFlow { map { Response(it.id, "SUCCESS", it) } } // Reusable response component

// Step 2: Compose workflow from reusable components
val mainWorkflow = validation + dbQuery + apiCall + mergeResults + successResponse

// Step 3: Create another workflow reusing the same components
val quickWorkflow = validation + dbQuery + successResponse // Reuse existing components

// Step 4: Execute workflows
val mainResult = mainWorkflow.execute(request).first()
val quickResult = quickWorkflow.execute(request).first()
```

### Component Reuse Benefits
- **Reduced Code Duplication**: Define components once, use them across multiple workflows
- **Consistent Behavior**: Ensure consistent validation, error handling, etc. across workflows
- **Easy Updates**: Modify a component once to update all workflows using it
- **Faster Development**: Build new workflows by assembling existing components

## 🔧 Getting Started

### Installation
Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.waylon:apexflow:1.0.0")
}
```

### Prerequisites
- Java 11+
- Kotlin 1.9+

## 🎨 Use Cases

- **Document Archiving**: Convert scanned documents to searchable PDFs
- **Workflow Automation**: Automated document processing pipelines
- **Microservices**: Lightweight document conversion services
- **Batch Processing**: Process large volumes of documents efficiently

## 📄 License

Apache License 2.0

---

Built with ❤️ using modern Kotlin technologies

ApexFlow - The future of document processing
