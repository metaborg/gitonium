---
title: "Gitonium"
---
# Gitonium
Gitonium is a Gradle plugin that derives the version number of the current project from the tags in its Git repository. Without configuration, Gitonium will find the last tag on the current branch that matches the pattern `release-*` and applies the derived version number to the current project and its subprojects.


## Requirements
This plugin requires Gradle 7 or newer.


## Apply
To apply this plugin, first add the Metaborg plugin repository to your `settings.gradle` file:

=== "Kotlin"
    ```kotlin title="settings.gradle.kts"
    pluginManagement {
        repositories {
            maven("https://artifacts.metaborg.org/content/repositories/releases/")
        }
    }
    ```

=== "Groovy"
    ```groovy title="settings.gradle"
    pluginManagement {
        repositories {
            maven {
                url "https://artifacts.metaborg.org/content/repositories/releases/"
            }
        }
    }
    ```

## Applying the plugin
Apply the gitonium plugin to a project (a `build.gradle(.kts)` file) as follows:

=== "Kotlin"
    ```kotlin title="build.gradle.kts"
    plugins {
        id("org.metaborg.gitonium") version "1.2.0"
    }
    ```

=== "Groovy"
    ```groovy title="build.gradle"
    plugins {
        id "org.metaborg.gitonium" version "1.2.0"
    }
    ```

