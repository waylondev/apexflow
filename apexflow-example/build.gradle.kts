plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    id("maven-publish")
    id("java-library")
}

group = "dev.waylon.apexflow"
version = "0.0.1"
description = "ApexFlow Example - TIFF to PDF Conversion Example"

// Configure Java 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Configure Kotlin to use JDK 21
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    mavenLocal() // Add local Maven repository to resolve apexflow modules
}

dependencies {
    // ApexFlow modules from local Maven repository
    implementation("dev.waylon.apexflow:apexflow-core:0.0.1")
    implementation("dev.waylon.apexflow:apexflow-tiff-twelvemonkeys:0.0.1")
    implementation("dev.waylon.apexflow:apexflow-pdf-pdfbox:0.0.1")
    implementation("dev.waylon.apexflow:apexflow-dsl-extensions:0.0.1")

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
                name.set("ApexFlow Example")
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
