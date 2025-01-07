group = "no.nav.pensjonsamhandling"

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    `maven-publish`
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.18.0")
    implementation("com.nimbusds", "nimbus-jose-jwt", "10.0.1")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.11.4")
    testImplementation("org.wiremock", "wiremock", "3.10.0")
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
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
    test {
        useJUnitPlatform()
    }
}
