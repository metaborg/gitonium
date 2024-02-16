plugins {
  id("org.metaborg.gradle.config.root-project") version "0.4.7"
  id("org.metaborg.gradle.config.java-library") version "0.4.7"
  id("org.metaborg.gitonium") version "0.1.5" // Bootstrap with previous version.
  kotlin("jvm") version "1.3.41" // 1.3.41 in sync with kotlin-dsl plugin.
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
}

gradlePlugin {
  plugins {
    create("gitonium") {
      id = "org.metaborg.gitonium"
      implementationClass = "mb.gitonium.GitoniumPlugin"
    }
  }
}

dependencies {
  implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
}
