import org.jetbrains.kotlin.gradle.tasks.*

group = "no.nav.pensjonsamhandling"

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    `maven-publish`
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.13.0")
    implementation("com.nimbusds", "nimbus-jose-jwt", "9.15.2")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.8.1")
    testImplementation("com.github.tomakehurst", "wiremock", "2.27.2")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            version = System.getenv("RELEASE_VERSION")
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/navikt/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }
    test {
        useJUnitPlatform()
    }
}
