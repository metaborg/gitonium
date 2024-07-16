plugins {
    `java-library`
    id("org.metaborg.gitonium")
}


group = "org.metaborg.example"

gitonium {
    mainBranch.set("master")
}

repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}
