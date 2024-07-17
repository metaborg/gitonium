plugins {
    `java-library`
    id("org.metaborg.gitonium")
}


group = "org.metaborg.example"

gitonium {
    mainBranch.set("master")
}
