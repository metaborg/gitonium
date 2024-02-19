plugins {
    `java-library`
    alias(libs.plugins.gitonium)        // Bootstrap with previous version.
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}


group = "org.metaborg"

repositories {
    mavenCentral()
}

dependencies {
    implementation      (libs.jgit)
    testImplementation  (libs.kotest)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

gradlePlugin {
    plugins {
        create("gitonium") {
            id = "org.metaborg.gitonium"
            implementationClass = "mb.gitonium.GitoniumPlugin"
        }
    }
}