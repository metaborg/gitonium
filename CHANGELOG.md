# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).


## [Unreleased]


## [1.7.2] - 2024-07-19
- Version computation is now lazy. Only assign the version to the project after configuring Gitonium.


## [1.7.1] - 2024-07-17
- No changes.


## [1.7.0] - 2024-07-16
- *Breaking*: The version must be explicitly set (if configuring Gitonium, set the version after configuring Gitonium):
    ```kotlin
    gitonium {
        mainBranch.set("main")
    }
    version = gitonium.version
    ```
- *Breaking*: Gitonium no longer applies the same policy to any subprojects automatically.
  If you have a multi-project build, you must apply Gitonium to each subproject individually and set the version for each project individually.


## [1.6.2] - 2024-07-16
- When creating a forced snapshot version, the snapshot version should be the same regardless of whether the current `HEAD` commit has a relwase tag or not.
- By default, set `firstParentOnly` to `true`.


## [1.6.1] - 2024-07-16
- No changes.


## [1.6.0] - 2024-07-16
- Add `gitonium.isSnapshot` Gradle property that, when set, forces the creation of a snapshot version even if the release tag is set to the current commit.


## [1.5.4] - 2024-07-15
- The Gitonium extension now consists of only lazy properties.


## [1.5.3] - 2024-07-15
- No changes.


## [1.5.2] - 2024-07-15
- Fix Gitonium extension properties being read too eagerly.


## [1.5.1] - 2024-07-15
- Small fix to extension conventions.
- Default value for `SNAPSHOT` suffix should not be preceded with a dash.


## [1.5.0] - 2024-07-15
- Don't include branch in snapshot version if it is the main branch (configurable with the `mainBranch` property).


## [1.4.1] - 2024-07-14
- Also record the latest release version in the properties file.


## [1.4.0] - 2024-07-14
- Add `writeBuildProperties` task that writes build and version information to a properties file. Example usage:
  ```kotlin
  gitonium {
      buildPropertiesFile.set(layout.buildDirectory.file("resources/main/version.properties"))
  }
  ```

## [1.3.1] - 2024-05-28
- No changes.

## [1.3.0] - 2024-05-28
- Add the `alwaysSnapshotVersion` option to `gitonium` to always create a snapshot version, even if HEAD points at a release tag.
  This can be used, for example, to create nightly snapshots.

## [1.2.0] - 2024-05-24
- By default, consider all branches for tags when finding a merge commit. Configurable with `firstParentOnly`.


## [1.1.0] - 2024-05-22
### Fixed
- Plugin can now be used without applying `maven-publish`.
- Fix `assertNotDirty` not detecting dirty versions properly.
- Fix `printVersion` eagerly printing the version when configured instead of when run.


## [1.0.0] - 2024-05-08
### Breaking Changes
- Gitonium now uses the native `git` command of the system (instead of JGit), and requires Git 2.32.0 (6 June 2021) or newer.
- Snapshot versions are (by default) one patch version higher than the last release.
  For example, if the last release was `1.0.0`, the snapshot version is `1.0.1-SNAPSHOT`
  (or `1.0.1-main-SNAPSHOT` when including the branch name). This removes the previous practice
  of setting the snapshot version to `999.9.9`.
- The `tagPattern` Regex member of the `gitonium` extension has been removed.
- The `tagPrefix` String member of the `gitonium` extension has been added. For example, instead of a `tagPattern` of `devenv-release/(.+)`, a `tagPrefix` of `devenv-release/` should be used.

### Added
- Add `printVersion` task that prints the current project version to STDOUT.
- Add `assertNotDirty` task that fails when trying to publish a 'dirty' version
  (i.e., a version with changes not in the current version tag).


## [0.1.5] - 2022-01-19
### Changed
- `SNAPSHOT` versions are prefixed with `999.9.9-` to ensure that `SNAPSHOT` versions are ordered above non-`SNAPSHOT` versions.
- `Project.DEFAULT_VERSION` (`"unspecified"`) is assigned as version if no Git repository is found, instead of failing.


[Unreleased]: https://github.com/metaborg/gitonium/compare/release-1.7.2...HEAD
[1.7.2]: https://github.com/metaborg/gitonium/compare/release-1.7.1...release-1.7.2
[1.7.1]: https://github.com/metaborg/gitonium/compare/release-1.7.0...release-1.7.1
[1.7.0]: https://github.com/metaborg/gitonium/compare/release-1.6.2...release-1.7.0
[1.6.2]: https://github.com/metaborg/gitonium/compare/release-1.6.1...release-1.6.2
[1.6.1]: https://github.com/metaborg/gitonium/compare/release-1.6.0...release-1.6.1
[1.6.0]: https://github.com/metaborg/gitonium/compare/release-1.5.4...release-1.6.0
[1.5.4]: https://github.com/metaborg/gitonium/compare/release-1.5.3...release-1.5.4
[1.5.3]: https://github.com/metaborg/gitonium/compare/release-1.5.2...release-1.5.3
[1.5.2]: https://github.com/metaborg/gitonium/compare/release-1.5.1...release-1.5.2
[1.5.1]: https://github.com/metaborg/gitonium/compare/release-1.5.0...release-1.5.1
[1.5.0]: https://github.com/metaborg/gitonium/compare/release-1.4.1...release-1.5.0
[1.4.1]: https://github.com/metaborg/gitonium/compare/release-1.4.0...release-1.4.1
[1.4.0]: https://github.com/metaborg/gitonium/compare/release-1.3.1...release-1.4.0
[1.3.1]: https://github.com/metaborg/gitonium/compare/release-1.3.0...release-1.3.1
[1.3.0]: https://github.com/metaborg/gitonium/compare/release-1.2.0...release-1.3.0
[1.2.0]: https://github.com/metaborg/gitonium/compare/release-1.1.0...release-1.2.0
[1.1.0]: https://github.com/metaborg/gitonium/compare/release-1.0.0...release-1.1.0
[1.0.0]: https://github.com/metaborg/gitonium/compare/release-0.1.5...release-1.0.0
[0.1.5]: https://github.com/metaborg/gitonium/compare/release-0.1.4...release-0.1.5
