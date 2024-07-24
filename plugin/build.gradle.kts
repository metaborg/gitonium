import org.metaborg.convention.MavenPublishConventionExtension
import org.metaborg.convention.Developer

plugins {
    `java-library`
    id("org.metaborg.gitonium") version "1.7.0"   // Bootstrap with a previous version
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    `kotlin-dsl`
    `java-gradle-plugin`
}

description = "A Git-based versioning plugin for Gradle."

dependencies {
    testImplementation  (libs.kotest)
    testImplementation  (libs.kotest.assertions)
    testImplementation  (libs.kotest.datatest)
    testImplementation  (libs.kotest.property)
}

gradlePlugin {
    website.set("https://github.com/metaborg/gitonium")
    vcsUrl.set("https://github.com/metaborg/gitonium")
    plugins {
        create("gitonium") {
            id = "org.metaborg.gitonium"
            implementationClass = "mb.gitonium.GitoniumPlugin"
        }
    }
}

allprojects {
    apply(plugin = "org.metaborg.gitonium")

    // Configure Gitonium before setting the version
    gitonium {
        mainBranch.set("master")
    }

    version = gitonium.version
    group = "org.metaborg"

    pluginManager.withPlugin("org.metaborg.convention.maven-publish") {
        extensions.configure(MavenPublishConventionExtension::class.java) {
            repoOwner.set("metaborg")
            repoName.set("gitonium")

            metadata {
                inceptionYear.set("2019")
                developers.set(listOf(
                    Developer("Virtlink", "Daniel A. A. Pelsmaeker", "developer@pelsmaeker.net"),
                    Developer("Gohla", "Gabriel Konat", "gabrielkonat@gmail.com"),
                ))
            }
        }
    }
}
