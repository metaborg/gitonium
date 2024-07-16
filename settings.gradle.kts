rootProject.name = "spoofax.gradle.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.6.12"
}


rootProject.name = "gitonium"

