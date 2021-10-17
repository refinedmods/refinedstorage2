# Contributing to Refined Storage

**IMPORTANT:** This project won't accept any code contributions at this time. The repository has been made public for
transparency, but it's too early to contribute for now.

## Versioning

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Pull requests

- Keep your PR as small as possible, this makes reviewing easier.
- Commits serve a clear purpose and have a fitting commit message.
- Branches are kept up to date by rebasing, preferably.
- PRs are merged by rebasing the commits on top of the target branch.
- Changes are added in `CHANGELOG.md`. Please refrain from using technical terminology, keep it user-friendly. The
  format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## Gitflow

This project uses [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

## Documentation

If you are adding functionality or are changing behavior, please update the wiki and Patchouli in-game documentation.

## Code style

Please use the `.editorconfig` file as provided.

## Testing

Our SonarQube Quality Gate requires a minimum test coverage percentage of 80%. This an aggregated percentage over all
the modules.

The `refinedstorage2-platform-fabric` module is excluded because this contains a lot of Minecraft-specific code and is
harder to test.

## Releasing

1) Make sure the version number in `build.gradle` is correct.
2) Merge `develop` to `main`.
3) Push a tag with the version number (prefixed with `v`).

After releasing:

1) Rename the "Unreleased" section to the correct version number in `CHANGELOG.md`.
2) Upgrade the version number in `build.gradle`.
3) Create a new "Unreleased" section in `CHANGELOG.md`.

## Pipelines

### Build

The build pipeline triggers when a commit is pushed to a branch or pull request.

All tests are run and an aggregated code coverage report is created. After that, a SonarQube analysis is run.

### Release

The release pipeline triggers when a tag is pushed. This will run all the steps that our build pipeline does.

After that succeeds, it will publish to GitHub packages.

The "Unreleased" section in `CHANGELOG.md` is parsed and a GitHub release is created with the changelog body and
relevant artifacts.

After that, a Discord notification is sent.

## Modules

Refined Storage 2 is modularized. That means that API is split up into various modules.

Important to note is that most modules aren't dependent on Minecraft or a mod loader. Only modules that start
with `refinedstorage2-platform-*` have dependencies on Minecraft.

|Name|Description|
|----|-----------|
|`refinedstorage2-core-api`|Contains some utility classes and enums.|
|`refinedstorage2-grid-api`|Contains Grid related functionality.|
|`refinedstorage2-network-api`|Contains storage networking related functionality.|
|`refinedstorage2-platform-fabric`|The platform module for Fabric. This module contains the playable mod and some internal API implementations. Don't depend on this when integrating!|
|`refinedstorage2-platform-fabric-api`|The API package for Fabric. Use this to integrate with Refined Storage on Fabric.|
|`refinedstorage2-query-parser`|A query parser. Contains a lexer and parser. Only used for Grid query parsing.|
|`refinedstorage2-resource-api`|Contains API for handling resources and resource lists.|
|`refinedstorage2-storage-api`|Contains storage related functionality.|
|`refinedstorage2-test`|This module is used in tests to provide the `@Rs2Test` annotation.|
