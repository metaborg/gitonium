plugins {
    `java-library`
    id("org.metaborg.gitonium") // Apply the plugin we're testing
}


group = "org.metaborg.example"

gitonium {
    mainBranch.set("master")
}
