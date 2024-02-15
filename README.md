
# Gitonium
[![Build][github-build-badge]][github-build]
[![License][license-badge]][license]
[![Maven Release][maven-release-badge]][maven-release]
[![GitHub Release][github-release-badge]][github-release]

Gitonium is a Gradle plugin for automatic versioning based on the current Git branch and/or tag.


## Requirements
Gradle 5 or higher is required. The code snippets in this README assume you are using Gradle with Kotlin, but should be translatable to Groovy as well.


## Prerequisites
The Gitonium plugin is not yet published to the Gradle plugins repository. Therefore, to enable downloading the plugin, add our repository to your `settings.gradle(.kts)` file:

```kotlin
pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/repositories/releases/")
    }
}
```


## Applying the plugin
Apply the gitonium plugin to a project (a `build.gradle(.kts)` file) as follows:

```kotlin
plugins {
    id("org.metaborg.gitonium") version("0.1.5")
}
```

The latest version of the plugin can be found at the top of this README. Gitonium will then lazily set the version of the project, and all sub-projects, to a version based on the Git repository. To override the version of a project, simply set the version as usual, and this will override the lazily set version.


## Versioning
Gitonium will check the current directory for a Git repository. If none is found in that directory, it will check its parent directory for one, and so on. If no Git repository is found, the plugin fails.

In the found Git repository, Gitonium will check if a release tag in the form of `release-{version}` points to the HEAD of the repository. If so, it will set the version of the project to `{version}`. For example, a tag `release-0.1.3` pointing to the HEAD will result in version `0.1.3`.

If no release tag was found, but the HEAD is on a branch, the version will be set to `999.9.9-{branch}-SNAPSHOT`. The `999.9.9` prefix to the version ensures that [Gradle always orders this version higher than regular release versions](https://docs.gradle.org/current/userguide/single_versions.html#version_ordering), and thus makes it possible to upgrade to it. For example, a HEAD on branch `master` will result in version `999.9.9-master-SNAPSHOT`.

If no release tag was found, and the HEAD is not on a branch, the version is not set and therefore defaults to Gradle's default version of `unspecified`.

If the repository has no HEAD, Gitonium will fail.


## Building
This repository is built with Gradle, which requires a JDK of at least version 8 to be installed. Higher versions may work depending on [which version of Gradle is used](https://docs.gradle.org/current/userguide/compatibility.html).

To build this repository, run `./gradlew buildAll` on Linux and macOS, or `gradlew buildAll` on Windows.

### Automated Builds
All branches and tags of this repository are built on:
- [GitHub actions](https://github.com/metaborg/gitonium/actions/workflows/build.yml) via `.github/workflows/build.yml`.
- Our [Jenkins buildfarm](https://buildfarm.metaborg.org/view/Devenv/job/metaborg/job/gitonium/) via `Jenkinsfile` which uses our [Jenkins pipeline library](https://github.com/metaborg/jenkins.pipeline/).


## License
Copyright (C) 2018-2024 Delft University of Technology

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[github-build-badge]: https://github.com/metaborg/gitonium/actions/workflows/build.yaml/badge.svg
[github-build]: https://github.com/metaborg/gitonium/actions
[license-badge]: https://img.shields.io/github/license/metaborg/gitonium
[license]: https://github.com/metaborg/gitonium/blob/main/LICENSE
[maven-release-badge]: https://img.shields.io/maven-metadata/v?label=maven-release&metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fgitonium%2Fmaven-metadata.xml
[maven-release]: https://mvnrepository.com/artifact/org.metaborg/gitonium
[github-release-badge]: https://img.shields.io/github/v/release/metaborg/gitonium
[github-release]: https://github.com/metaborg/gitonium/releases
