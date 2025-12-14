plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
    id("java-library")
}

group = "dev.waylon.apexflow"
version = "0.0.1"
description = "ApexFlow TIFF-TwelveMonkeys Library - TIFF reading functionality using TwelveMonkeys ImageIO"

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
    mavenLocal() // Add local Maven repository to resolve core module
}


dependencies {
    // Core dependencies - using version catalog for consistency
    implementation(libs.kotlinx.coroutines.core)

    // Add core module dependency from local Maven repository
    implementation("dev.waylon.apexflow:apexflow-core:0.0.1")

    implementation(libs.twelvemonkeys.imageio.tiff)

    testImplementation(libs.kotlin.test)
}

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
                name.set("ApexFlow TIFF-TwelveMonkeys")
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
