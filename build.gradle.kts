plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.12"
  id("org.metaborg.gitonium") version "0.1.2" // Bootstrap with previous version.
  kotlin("jvm") version "1.3.61"
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
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
  implementation("org.eclipse.jgit:org.eclipse.jgit:5.3.1.201904271842-r")
}
