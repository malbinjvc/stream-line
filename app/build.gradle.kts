plugins {
    alias(libs.plugins.kotlin.jvm)
    id("org.springframework.boot") version "3.5.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.spring") version "2.3.10"
    kotlin("plugin.jpa") version "2.3.10"
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.springframework.security:spring-security-test")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("stream-line.jar")
}
