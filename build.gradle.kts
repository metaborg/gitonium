plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.6"
  id("org.metaborg.gitonium") version "0.1.0" // Bootstrap with previous version.
  kotlin("jvm") version "1.3.21"
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
}

dependencies {
  compile("org.eclipse.jgit:org.eclipse.jgit:5.2.0.201812061821-r")
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
