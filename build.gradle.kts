import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "3.0.1"
    id("com.gradleup.shadow") version "8.3.5"
    application
}

group = "com.traderepublic.ytm.stream"
version = "1.0"

repositories {
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.coroutines.core)
    implementation(libs.bundles.kafka)
    implementation(libs.redisson)
    implementation(libs.bundles.money)
    implementation(libs.bundles.logging)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
        attributes["Class-Path"] = configurations.compileClasspath.get().joinToString(" ") { it.name }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion))
    }
}

tasks.compileKotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<ShadowJar>("shadowJar") {
    archiveBaseName.set("ytm-stream")
    archiveClassifier.set("")
    archiveVersion.set("")
}