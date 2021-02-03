[![GitHub license](https://img.shields.io/github/license/metaborg/gitonium)](https://github.com/metaborg/gitonium/blob/master/LICENSE)
[![Jenkins](https://img.shields.io/jenkins/build/https/buildfarm.metaborg.org/job/metaborg/job/gitonium/job/master)](https://buildfarm.metaborg.org/job/metaborg/job/gitonium/job/master/lastBuild)
[![coronium](https://img.shields.io/maven-metadata/v?label=gitonium&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fgitonium%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/org.metaborg/gitonium?repo=metaborg-releases)


# Gitonium

Gitonium is a Gradle plugin for automatic versioning based on the current Git branch and/or tag.

## Requirements

Gradle 5 or higher is required.
The code snippets in this README assume you are using Gradle with Kotlin, but should be translatable to Groovy as well.

## Prerequisites

The Gitonium plugin is not yet published to the Gradle plugins repository.
Therefore, to enable downloading the plugin, add our repository to your settings.gradle(.kts) file:

```kotlin
pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
  }
}
```

## Applying the plugin

Apply the gitonium plugin to a project (a build.gradle(.kts) file) as follows:

```kotlin
plugins {
  id("org.metaborg.gitonium") version("0.1.4")
}
```

The latest version of the plugin can be found at the top of this readme.

Gitonium will then lazily set the version of the project, and all sub-projects, to a version based on the Git repository.
To override the version of a project, simply set the version as usual, and this will override the lazily set version.

## Versioning

Gitonium will check the current directory for a Git repository.
If none is found in that directory, it will check its parent directory for one, and so on.
If no Git repository is found, the plugin fails.

In the found Git repository, Gitonium will check if a release tag in the form of `release-{version}` points to the HEAD of the repository.
If so, it will set the version of the project to `{version}`.
For example, a tag `release-0.1.3` pointing to the HEAD will result in version `0.1.3`.

If no release tag was found, but the HEAD is on a branch, the version will be set to `{branch}-SNAPSHOT`.
For example, a HEAD on branch `master` will result in version `master-SNAPSHOT`.

If no release tag was found, and the HEAD is not on a branch, the version is not set and therefore defaults to Gradle's default version of `unspecified`.

If the repository has no HEAD, Gitonium will fail.
