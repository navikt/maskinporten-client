import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

group = "no.nav.security"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.11.2")
    implementation("com.nimbusds", "nimbus-jose-jwt", "9.0.1")
    implementation("org.junit.jupiter", "junit-jupiter", "5.7.0")
    testImplementation(kotlin("test-junit5"))
    testImplementation("com.github.tomakehurst", "wiremock", "2.27.2")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "14"
    }
    test {
        useJUnitPlatform()
    }
}