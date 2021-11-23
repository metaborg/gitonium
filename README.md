[![GitHub license](https://img.shields.io/github/license/metaborg/gitonium)](https://github.com/metaborg/gitonium/blob/master/LICENSE)
[![GitHub actions](https://img.shields.io/github/workflow/status/metaborg/gitonium/Build?label=GitHub%20actions)](https://github.com/metaborg/common/actions/workflows/build.yml)
[![Jenkins](https://img.shields.io/jenkins/build/https/buildfarm.metaborg.org/job/metaborg/job/gitonium/job/master?label=Jenkins)](https://buildfarm.metaborg.org/job/metaborg/job/gitonium/job/master/lastBuild)
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

## Development

This section details the development of this project.

### Building

This repository is built with Gradle, which requires a JDK of at least version 8 to be installed. Higher versions may work depending on [which version of Gradle is used](https://docs.gradle.org/current/userguide/compatibility.html).

To build this repository, run `./gradlew buildAll` on Linux and macOS, or `gradlew buildAll` on Windows.

### Automated Builds

All branches and tags of this repository are built on:
- [GitHub actions](https://github.com/metaborg/gitonium/actions/workflows/build.yml) via `.github/workflows/build.yml`.
- Our [Jenkins buildfarm](https://buildfarm.metaborg.org/view/Devenv/job/metaborg/job/gitonium/) via `Jenkinsfile` which uses our [Jenkins pipeline library](https://github.com/metaborg/jenkins.pipeline/).

### Publishing

This repository is published via Gradle and Git with the [Gitonium](https://github.com/metaborg/gitonium) and [Gradle Config](https://github.com/metaborg/gradle.config) plugins.
It is published to our [artifact server](https://artifacts.metaborg.org) in the [releases repository](https://artifacts.metaborg.org/content/repositories/releases/).

First update `CHANGELOG.md` with your changes, create a new release entry, and update the release links at the bottom of the file.
Then, commit your changes.

To make a new release, create a tag in the form of `release-*` where `*` is the version of the release you'd like to make.
Then first build the project with `./gradlew buildAll` to check if building succeeds.

If you want our buildfarm to publish this release, just push the tag you just made, and our buildfarm will build the repository and publish the release.

If you want to publish this release locally, you will need an account with write access to our artifact server, and tell Gradle about this account.
Create the `./gradle/gradle.properties` file if it does not exist.
Add the following lines to it, replacing `<username>` and `<password>` with those of your artifact server account:
```
publish.repository.metaborg.artifacts.username=<username>
publish.repository.metaborg.artifacts.password=<password>
```
Then run `./gradlew publishAll` to publish all built artifacts.
You should also push the release tag you made such that this release is reproducible by others.

## Copyright and License

Copyright © 2018-2021 Delft University of Technology

The files in this repository are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
You may use the files in this repository in compliance with the license.
