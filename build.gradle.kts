plugins {
    `java-library`
    id("org.metaborg.gitonium") version "0.1.5"   // Bootstrap with previous version. Must match version in libs.versions.toml
    kotlin("jvm") version "1.7.10"                // Must match version in libs.versions.toml
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}


group = "org.metaborg"

repositories {
    mavenCentral()
}

dependencies {
    implementation      ("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")     // Must match version in libs.versions.toml
    testImplementation  ("io.kotest:kotest-runner-junit5:5.8.0")                        // Must match version in libs.versions.toml
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

gradlePlugin {
    plugins {
        create("gitonium") {
            id = "org.metaborg.gitonium"
            implementationClass = "mb.gitonium.GitoniumPlugin"
        }
    }
}


// Build tasks
tasks.register("assembleAll") {
    group = "Build"
    description = "Assembles the outputs of the subprojects and included builds."
    dependsOn("assemble")
    dependsOn(gradle.includedBuilds.map { it.task(":assemble") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("assemble") })
}
tasks.register("buildAll") {
    group = "Build"
    description = "Assembles and tests the subprojects and included builds."
    dependsOn("build")
    dependsOn(gradle.includedBuilds.map { it.task(":build") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("build") })
}
tasks.register("cleanAll") {
    group = "Build"
    description = "Cleans the outputs of the subprojects and included builds."
    dependsOn("clean")
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("clean") })
}

// Publishing tasks
tasks.register("publishAll") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to a remote Maven repository."
    dependsOn("publish")
    dependsOn(gradle.includedBuilds.map { it.task(":publish") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("publish") })
}
tasks.register("publishAllToMavenLocal") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to the local Maven repository."
    dependsOn("publishToMavenLocal")
    dependsOn(gradle.includedBuilds.map { it.task(":publishToMavenLocal") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("publishToMavenLocal") })
}

// Verification tasks
tasks.register("checkAll") {
    group = "Verification"
    description = "Runs all checks on the subprojects and included builds."
    dependsOn("check")
    dependsOn(gradle.includedBuilds.map { it.task(":check") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("check") })
}
tasks.register("testAll") {
    group = "Verification"
    description = "Runs all unit tests on the subprojects and included builds."
    dependsOn("test")
    dependsOn(gradle.includedBuilds.map { it.task(":test") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("test") })
}

// Help tasks
tasks.register("allTasks") {
    group = "Help"
    description = "Displays all tasks of subprojects and included builds."
    dependsOn("tasks")
    dependsOn(gradle.includedBuilds.map { it.task(":tasks") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("tasks") })
}
