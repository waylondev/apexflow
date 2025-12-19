plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
    id("java-library")
    id("org.jetbrains.kotlinx.benchmark") version "0.5.0"
}

group = "dev.waylon.apexflow"
version = "0.0.2"

description = "ApexFlow PDF-PDFBox Library - PDF writing functionality using PDFBox"

// Configure Java 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Configure Kotlin to use JDK 21
kotlin {
    jvmToolchain(21)
}

// Configure Kotlin compiler options for context parameters
val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.addAll(listOf("-Xcontext-parameters", "-Xskip-prerelease-check"))
    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
}

// Also configure test Kotlin compiler options
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.compilerOptions {
    freeCompilerArgs.addAll(listOf("-Xskip-prerelease-check"))
    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
}

repositories {
    mavenCentral()
    mavenLocal() // Add local Maven repository to resolve core module
}

dependencies {
    // Core dependencies - using version catalog for consistency
    implementation(libs.kotlinx.coroutines.core)

    // Add core module dependency from local Maven repository
    implementation("dev.waylon.apexflow:apexflow-core:0.0.2")

    // PDFBox dependencies
    implementation(libs.pdfbox)
    implementation(libs.twelvemonkeys.imageio.tiff)

    // Logging dependencies
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)

    // Test dependencies
    testImplementation(libs.kotlin.test)
    
    // Benchmark dependencies
    benchmarkImplementation(libs.kotlinx.benchmark.runtime)
    benchmarkImplementation(kotlin("test"))
}

// Use the Kotlin test runner instead of JUnit to avoid dependency issues
tasks.test {
    useJUnitPlatform()
    // Increase heap size for testing large files
    jvmArgs = listOf("-Xmx4g", "-Xms2g")
}

// Benchmark configuration
benchmark {
    targets {
        register("main") {
            mode = "throughput"
            iterations = 5
            warmups = 2
            timeUnit = "ms"
        }
    }
    configurations {
        named("main") {
            jvmArgs = listOf("-Xmx4g", "-Xms2g")
        }
    }
}

// Add benchmark source set
sourceSets {
    create("benchmark") {
        kotlin {
            srcDir("src/benchmark/kotlin")
        }
        resources {
            srcDir("src/benchmark/resources")
        }
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

// Configure benchmark compilation
val compileBenchmarkKotlin by tasks.getting(JavaCompile::class) {
    dependsOn(compileKotlin)
}

// Configure Maven publishing
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/waylondev/apexflow")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("ApexFlow PDF-PDFBox")
                description.set(project.description)
                url.set("https://github.com/waylondev/apexflow")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("waylondev")
                        name.set("Waylon Developer")
                        email.set("developer@waylon.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/waylondev/apexflow.git")
                    developerConnection.set("scm:git:ssh://github.com:waylondev/apexflow.git")
                    url.set("https://github.com/waylondev/apexflow")
                }
            }
        }
    }
}