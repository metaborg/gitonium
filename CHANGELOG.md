# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).


## [Unreleased]

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


[Unreleased]: https://github.com/metaborg/gitonium/compare/release-1.3.0...HEAD
[1.3.0]: https://github.com/metaborg/gitonium/compare/release-1.2.0...release-1.3.0
[1.2.0]: https://github.com/metaborg/gitonium/compare/release-1.1.0...release-1.2.0
[1.1.0]: https://github.com/metaborg/gitonium/compare/release-1.0.0...release-1.1.0
[1.0.0]: https://github.com/metaborg/gitonium/compare/release-0.1.5...release-1.0.0
[0.1.5]: https://github.com/metaborg/gitonium/compare/release-0.1.4...release-0.1.5
