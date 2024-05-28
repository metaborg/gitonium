import java.net.URI

// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    alias(libs.plugins.gitonium)   // Bootstrap with previous version
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}


group = "org.metaborg"
description = "A Git-based versioning plugin for Gradle."
extra["isReleaseVersion"] = !version.toString().endsWith("-SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation  (libs.kotest)
    testImplementation  (libs.kotest.assertions)
    testImplementation  (libs.kotest.datatest)
    testImplementation  (libs.kotest.property)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
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

publishing {
    afterEvaluate {
        publications {
            withType<MavenPublication> {
                pom {
                    name.set("Gitonium")
                    description.set(project.description)
                    url.set("https://github.com/metaborg/gitonium")
                    inceptionYear.set("2023")
                    licenses {
                        // From: https://spdx.org/licenses/
                        license {
                            name.set("Apache-2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("gohla")
                            name.set("Gabriel Konat")
                            email.set("g.d.p.konat@tudelft.nl")
                        }
                        developer {
                            id.set("virtlink")
                            name.set("Daniel A. A. Pelsmaeker")
                            email.set("d.a.a.pelsmaeker@tudelft.nl")
                        }
                    }
                    scm {
                        connection.set("scm:git@github.com:metaborg/gitonium.git")
                        developerConnection.set("scm:git@github.com:metaborg/gitonium.git")
                        url.set("scm:git@github.com:metaborg/gitonium.git")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://artifacts.metaborg.org/content/repositories/releases/")
            val snapshotsRepoUrl = uri("https://artifacts.metaborg.org/content/repositories/snapshots/")
            name = "MetaborgArtifacts"
            url = if (project.extra["isReleaseVersion"] as Boolean) releasesRepoUrl else snapshotsRepoUrl
            credentials {
                username = project.findProperty("publish.repository.metaborg.artifacts.username") as String? ?: System.getenv("METABORG_ARTIFACTS_USERNAME")
                password = project.findProperty("publish.repository.metaborg.artifacts.password") as String? ?: System.getenv("METABORG_ARTIFACTS_PASSWORD")
            }
        }
    }
}


// Normally, when you execute a task such as `test` in a multi-project build, you will execute
//  all `:test` tasks in all projects. In contrast, when you specifically execute `:test`
//  (prefixed with a colon), you execute the `:test` task only in the root project.
// Now, we would like to create a task in this composite build `testAll` that executes basically
//  the equivalent of `test` in each of the included multi-project builds, which would execute
//  `:test` in each of the projects. However, this seems to be impossible to write down. Instead,
//  we call the root `:test` task in the included build, and in each included build's multi-project
//  root project we'll extend the `test` task to depend on the `:test` tasks of the subprojects.

// Build tasks
tasks.register("assembleAll") {
    group = "Build"
    description = "Assembles the outputs of the subprojects and included builds."
    dependsOn(tasks.named("assemble"))
    dependsOn(gradle.includedBuilds.map { it.task(":assemble") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("assemble") })
}
tasks.register("buildAll") {
    group = "Build"
    description = "Assembles and tests the subprojects and included builds."
    dependsOn(tasks.named("build"))
    dependsOn(gradle.includedBuilds.map { it.task(":build") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("build") })
}
tasks.register("cleanAll") {
    group = "Build"
    description = "Cleans the outputs of the subprojects and included builds."
    dependsOn(tasks.named("clean"))
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("clean") })
}

// Publishing tasks
tasks.register("publishAll") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to a remote Maven repository."
    dependsOn(tasks.named("publish"))
    dependsOn(gradle.includedBuilds.map { it.task(":publish") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("publish") })
}
tasks.register("publishAllToMavenLocal") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to the local Maven repository."
    dependsOn(tasks.named("publishToMavenLocal"))
    dependsOn(gradle.includedBuilds.map { it.task(":publishToMavenLocal") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("publishToMavenLocal") })
}

// Verification tasks
tasks.register("checkAll") {
    group = "Verification"
    description = "Runs all checks on the subprojects and included builds."
    dependsOn(tasks.named("check"))
    dependsOn(gradle.includedBuilds.map { it.task(":check") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("check") })
}
tasks.register("testAll") {
    group = "Verification"
    description = "Runs all unit tests on the subprojects and included builds."
    dependsOn(tasks.named("test"))
    dependsOn(gradle.includedBuilds.map { it.task(":test") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("test") })
}

// Help tasks
tasks.register("allTasks") {
    group = "Help"
    description = "Displays all tasks of subprojects and included builds."
    dependsOn(tasks.named("tasks"))
    dependsOn(gradle.includedBuilds.map { it.task(":tasks") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("tasks") })
}

