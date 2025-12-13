plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "dev.waylon.apexflow"
version = "0.0.1"
description = "ApexFlow Example - TIFF to PDF Conversion Example"

repositories {
    mavenCentral()
    mavenLocal() // Add local Maven repository to resolve apexflow modules
}

dependencies {
    // ApexFlow modules from local Maven repository
    implementation("dev.waylon.apexflow:apexflow-core:0.0.1")
    implementation("dev.waylon.apexflow:apexflow-tiff-twelvemonkeys:0.0.1")
    implementation("dev.waylon.apexflow:apexflow-pdf-pdfbox:0.0.1")

    // Add coroutines dependency
    implementation(libs.kotlinx.coroutines.core)

    // Logging implementation for example app
    implementation(libs.logback.classic)

    // Test dependencies
    testImplementation(libs.kotlin.test)
}

// Application plugin configuration
application {
    // Set a default main class
    mainClass.set("dev.waylon.apexflow.example.BasicTiffToPdfConverterKt")
}

// Default run task - run the basic example
application {
    mainClass.set("dev.waylon.apexflow.example.BasicTiffToPdfConverterKt")
}

// Use the Kotlin test runner instead of JUnit to avoid dependency issues
tasks.test {
    useJUnitPlatform()
}
