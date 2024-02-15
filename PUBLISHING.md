# Publishing
How to publish a new version of this library?

Refer to [the setup](#setup) on how to setup the repository and your system for publishing.


## Automatic Publishing
Automatic publishing is preferred over manual publishing. To publish a new version of the library automatically, follow these steps:

1.  Update the `CHANGELOG.md` with the latest changes, create a new release entry, and update the release links at the bottom of the file.
2.  Commit your changes and ensure there are no new or changed files in the repository.
3.  Use Git to create a tag with the new version number and the `release-` prefix, such as `release-0.1.2-alpha`.
    ```shell
    git tag release-0.1.2-alpha
    ```
4.  Verify that the version number is correctly picked up using:
    ```shell
    ./gradlew printVersion
    ```
    The version number should not contain a commit hash and should not end with `.dirty`.
5.  Push the tag to the remote repository:
    ```shell
    git push origin release-0.1.2-alpha
    ```
6.  Update the `README.md` and documentation to reflect the latest release of the library.


## Manual Publishing
To publish a new version of the library manually from the command-line, follow the above steps and then:

1.  Build, sign, and publish the artifact using:
    ```shell
    ./gradlew publishAll
    ```
2.  Update the `README.md` and documentation to reflect the latest release of the library.


## Setup
In your _home_ directory at `~/gradle.properties`, ensure the file contains properties for publishing to our [artifact server](https://artifacts.metaborg.org). Replace `<username>` and `<password>` with those of your artifact server account

```properties
# Metaborg artifact publishing
publish.repository.metaborg.artifacts.username=<username>
publish.repository.metaborg.artifacts.password=<password>
```

