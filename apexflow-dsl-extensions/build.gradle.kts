plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.waylon.apexflow"
version = "0.0.1"
description = "ApexFlow DSL Extensions - Concise API for common conversion tasks"

repositories {
    mavenCentral()
}

dependencies {
    // ApexFlow modules from local Maven repository
    implementation("dev.waylon.apexflow:apexflow-core:0.0.1")
    implementation("dev.waylon.apexflow:apexflow-pdf-pdfbox:0.0.1")
    implementation("dev.waylon.apexflow:apexflow-tiff-twelvemonkeys:0.0.1")

    // Add coroutines dependency
    implementation(libs.kotlinx.coroutines.core)

    // Test dependencies
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Use the Kotlin test runner
tasks.test {
    useJUnitPlatform()
}
