import jdk.tools.jlink.resources.plugins

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-library")
    id("maven-publish")
}

group = "dev.waylon.apexflow"
version = "0.0.1"
description = "ApexFlow Core Library - High Performance TIFF to PDF Conversion Engine"

// Configure Java 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}


dependencies {
    // Core dependencies - using version catalog for consistency
    implementation(libs.kotlinx.coroutines.core)

    // Test dependencies - Kotlin testing framework
    testImplementation(libs.kotlin.test)

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
                name.set("ApexFlow Core")
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

