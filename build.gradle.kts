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

gradlePlugin {
    plugins {
        create("gitonium") {
            id = "org.metaborg.gitonium"
            implementationClass = "mb.gitonium.GitoniumPlugin"
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

