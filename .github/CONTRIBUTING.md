# Contributing to Refined Storage

> This project won't accept any code contributions at this time. The repository has been made public for
> transparency, but it's too early to contribute for now.

## Versioning

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

When porting to a new (binary-breaking) Minecraft version, the minor version number needs to be incremented.
That way, we can still keep maintaining the previous Minecraft version if necessary without being in conflict
with the version number of the new Minecraft version.

## Pull requests

- Keep your PR as small as possible, this makes reviewing easier.
- Commits serve a clear purpose and have a fitting commit message.
- Branches are kept up to date by rebasing, preferably.
- PRs are merged by rebasing the commits on top of the target branch.
- Remember to add your changes in `CHANGELOG.md`.

## Changelog

The changelog is kept in `CHANGELOG.md`.

Keeping a readable, relevant and user-friendly changelog is essential for our end users
to stay up to date with the project.

Please refrain from using technical terminology or adding entries for technical changes
that are (generally) not relevant to the end-user.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## Gitflow

This project uses [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

We use [support branches](https://groups.google.com/g/gitflow-users/c/I9sErOSzYzE/m/AwVH06CuKT0J) for supporting different major/minor versions at the same time.

## Documentation

Documentation must be kept up to date when adding functionality or changing documentation.

## Code style

We use [Checkstyle](https://checkstyle.sourceforge.io/) in our build pipeline to validate coding style.

It is recommended to import the `checkstyle.xml` config into your IDE and/or install a Checkstyle plugin.

## Testing

When adding functionality or fixing a bug, it is important to add tests. Tests are important, if not more important,
than the implementation code.

That means that they need to be first class citizens in the codebase, and must be readable
at all times.

They ensure that there are no regressions, act as general documentation for the codebase,
and ensure that the project can be evolved over time.

### Test coverage

Our [SonarQube quality gate](https://sonarcloud.io/organizations/refinedmods/quality_gates/show/9) requires a minimum
test coverage percentage of 80%. This an aggregated percentage over all
the modules, with an exclusion for the platform modules.

The `refinedstorage2-platform-*` modules are excluded because they contain a lot of Minecraft-specific code and is
harder to test.

### Mutation testing

We also have [Pitest mutation testing](https://pitest.org/), which requires a minimum test coverage percentage of 80%
and a minimum mutation
percentage of 90%.

## Releasing

1) Make sure the version number in `build.gradle` is correct.
2) Merge `develop` to `main` (with a merge commit, **NOT** by rebasing as rebasing messes up further release merges).
3) Push a tag with the version number (prefixed with `v`).

After releasing:

1) Rename the "Unreleased" section to the correct version number in `CHANGELOG.md`.
2) Upgrade the version number in `build.gradle`.
3) Create a new "Unreleased" section in `CHANGELOG.md`.

## Pipelines

### Build

The build pipeline triggers when a commit is pushed to a branch or pull request.

All tests are run and an aggregated code coverage report is created. After that, a SonarQube analysis is run.

Pitest is run to ensure mutation coverage.

### Release

The release pipeline triggers when a tag is pushed. This will run all the steps that our build pipeline does.

After that succeeds, it will publish to GitHub packages.

The "Unreleased" section in `CHANGELOG.md` is parsed and a GitHub release is created with the changelog body and
relevant artifacts.

After that, a Discord and Twitter notification is sent.

## Modules

Refined Storage 2 is modularized. That means that the project is split up into various modules.

Important to note is that most modules aren't dependent on Minecraft or a mod loader. Only modules that start
with `refinedstorage2-platform-*` have dependencies on Minecraft.

| Name                              | Use in addons | Description                                                                           |
|-----------------------------------|---------------|---------------------------------------------------------------------------------------|
| `refinedstorage2-core-api`        | ✔️            | Contains some utility classes and enums.                                              |
| `refinedstorage2-grid-api`        | ✔️            | Contains Grid related functionality.                                                  |
| `refinedstorage2-network-api`     | ✔️            | Contains storage network related functionality.                                       |
| `refinedstorage2-network-api`     | ✔️            | Contains storage network related functionality.                                       |
| `refinedstorage2-network-test`    | ✔️            | JUnit extension which helps with setting up a network and a network node for testing. |
| `refinedstorage2-query-parser`    | ✔️            | A query parser, contains a lexer and parser. Only used for Grid query parsing.        |
| `refinedstorage2-resource-api`    | ✔️            | Contains API for handling resources.                                                  |
| `refinedstorage2-storage-api`     | ✔️            | Contains storage related functionality.                                               |
| `refinedstorage2-platform-api`    | ✔️            | Implements the various Refined Storage API modules for use in Minecraft.              |
| `refinedstorage2-platform-fabric` | ❌             | The platform module for Fabric. This module contains Fabric specific code.            |
| `refinedstorage2-platform-forge`  | ❌             | The platform module for Forge. This module contains Forge specific code.              |
| `refinedstorage2-platform-common` | ❌             | Common mod code. Most gameplay code is in here.                                       |
| `refinedstorage2-platform-test`   | ❌             | This module is used in platform tests for various Minecraft related helpers.          |
