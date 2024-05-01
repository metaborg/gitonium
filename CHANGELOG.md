# Changelog
All notable changes to this project are documented in this file, based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).


## [Unreleased]
### Breaking Changes
- Gitonium now uses the native `git` command of the system (instead of JGit), and requires Git 2.32.0 (6 June 2021) or newer.
- Snapshot versions are (by default) one patch version higher than the last release.
  For example, if the last release was `1.0.0`, the snapshot version is `1.0.1-SNAPSHOT`
  (or `1.0.1-main-SNAPSHOT` when including the branch name). This removes the previous practice
  of setting the snapshot version to `999.9.9`.


## [0.1.5] - 2022-01-19
### Changed
- `SNAPSHOT` versions are prefixed with `999.9.9-` to ensure that `SNAPSHOT` versions are ordered above non-`SNAPSHOT` versions.
- `Project.DEFAULT_VERSION` (`"unspecified"`) is assigned as version if no Git repository is found, instead of failing.


[Unreleased]: https://github.com/metaborg/gitonium/compare/release-0.1.5...HEAD
[0.1.5]: https://github.com/metaborg/gitonium/compare/release-0.1.4...release-0.1.5
