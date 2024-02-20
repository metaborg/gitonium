import org.gradle.util.GradleVersion

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

plugins {
    if(GradleVersion.current() >= GradleVersion.version("7.6")) {
            id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
    }
}

rootProject.name = "gitonium"
