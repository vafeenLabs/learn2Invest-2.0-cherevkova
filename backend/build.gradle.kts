plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    kotlin("plugin.serialization") version "1.9.0"
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    //ktor
// Ktor Core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    // Content Negotiation (JSON)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)

    // Дополнительные фичи
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.compression)

    // Логирование (обязательно для работы)
    implementation(libs.logback.classic)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
}
