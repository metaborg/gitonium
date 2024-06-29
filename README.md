# Gitonium
[![Build][github-build-badge]][github-build]
[![License][license-badge]][license]
[![GitHub Release][github-release-badge]][github-release]
[![Documentation][documentation-badge]][documentation]

Gitonium is a Gradle plugin for automatic versioning based on the current Git branch and/or tag.

[![Documentation][documentation-button]][documentation]

| Artifact                        | Latest Release                                       |
|---------------------------------|------------------------------------------------------|
| `org.metaborg.gitonium`         | [![org.metaborg.gitonium][maven-badge]][maven]       |


## Requirements
Gradle 7 or higher is required. The code snippets in this README assume you are using Gradle with Kotlin, but should be translatable to Groovy as well.


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
    id("org.metaborg.gitonium") version "1.3.1"
}
```

The latest version of the plugin can be found at the top of this README. Gitonium will then lazily set the version of the project, and all sub-projects, to a version based on the Git repository. To override the version of a project, simply set the version manually, and this will override the lazily set version.


## Usage
Gitonium sets the version of the project based on the latest version tag (of the form `release-{version}`) found on the current branch. For example, if the HEAD of the current branch is tagged `release-0.1.3`, then the version is assigned `0.1.3`. Alternatively, if an earlier commit on the `main` branch is tagged `release-0.1.3`, then the version `0.1.4-main-SNAPSHOT` is assigned, one patch version higher than the last release.

If no release tag was found, the version is not set and therefore defaults to Gradle's default version of `unspecified`.

If the repository has no HEAD, Gitonium will fail.

> [!IMPORTANT]
> A shallow clone of a repository (as commonly performed by CI) might not checkout the tags of the repository.
> In this case, Gitonium will fail to find the version tag.
>
> For example, when using GitHub CI `actions/checkout` action, specify a non-shallow checkout including tags:
>
> ```yaml
> - name: Checkout
>   uses: actions/checkout@v4
>   with:
>     fetch-depth: 0
>     fetch-tags: true
>  ```



## License
Copyright 2018-2024 Delft University of Technology

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[github-build-badge]: https://img.shields.io/github/actions/workflow/status/metaborg/gitonium/build.yaml
[github-build]: https://github.com/metaborg/gitonium/actions
[license-badge]: https://img.shields.io/github/license/metaborg/gitonium
[license]: https://github.com/metaborg/gitonium/blob/main/LICENSE
[github-release-badge]: https://img.shields.io/github/v/release/metaborg/gitonium
[github-release]: https://github.com/metaborg/gitonium/releases
[documentation-badge]: https://img.shields.io/badge/docs-latest-brightgreen
[documentation]: https://spoofax.dev/gitonium/
[documentation-button]: https://img.shields.io/badge/Documentation-blue?style=for-the-badge&logo=googledocs&logoColor=white

[maven-badge]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fartifacts.metaborg.org%2Fcontent%2Frepositories%2Freleases%2Forg%2Fmetaborg%2Fgitonium%2Fmaven-metadata.xml
[maven]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg~gitonium~~~

