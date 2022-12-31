# Contributing to Refined Storage

When contributing to this repository, please first discuss the change you wish to make via
[GitHub issues](https://github.com/refinedmods/refinedstorage2/issues), [Discord](https://discordapp.com/invite/VYzsydb),
or any other method with the owners of this repository before making a change.

## Pull requests

- Keep your pull request (PR) as small as possible, this makes reviewing easier.
- Commits serve a clear purpose and have a fitting commit message.
- Branches are kept up to date by rebasing.
- PRs are merged by rebasing the commits on top of the target branch (which is `develop`).
- Remember to add your changes in `CHANGELOG.md`. If your changes are merely technical, it's not necessary to update the
  changelog as it's not relevant for users.

### Commit messages

Commit messages must adhere to [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). We
use [Commitlint](https://commitlint.js.org/) in pull request pipelines to validate commit messages.

It is recommended to install
the [Conventional Commit plugin](https://plugins.jetbrains.com/plugin/13389-conventional-commit) to make it
easier to write commit messages.

## Versioning

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

When porting to a new (binary-breaking) Minecraft version, the minor version number needs to be incremented.
That way, we can still keep maintaining the previous Minecraft version if necessary without being in conflict
with the version number of the new Minecraft version.

## Changelog

The changelog is kept in `CHANGELOG.md`.

Keeping a readable, relevant and user-friendly changelog is essential for our end users
to stay up to date with the project.

Please refrain from using technical terminology or adding entries for technical changes
that are (generally) not relevant to the end-user.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## Gitflow

This project uses [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

We use [support branches](https://groups.google.com/g/gitflow-users/c/I9sErOSzYzE/m/AwVH06CuKT0J) for supporting
different major/minor versions at the same time.

## Documentation

Documentation must be kept up to date when adding or changing functionality.

### Javadoc

Javadoc is available after every release on https://refinedmods.com/refinedstorage2/.

## Code style

We use [Checkstyle](https://checkstyle.sourceforge.io/) in our build pipeline to validate coding style.

It is recommended to import the [config/checkstyle/checkstyle.xml](../config/checkstyle/checkstyle.xml) file into your
IDE, so that formatting rules are respected.

Moreover, the [CheckStyle-IDEA plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea) can be used to check
if there are no style violations.

## Architecture

### Architecture Decision Records

The ADRs of this project can be found under [doc/architecture/decision](../doc/architecture/decision).

## Testing

When adding functionality or fixing a bug, it is important to add tests. Tests are important, if not more important,
than the implementation code.

That means that they need to be first class citizens in the codebase, and must be readable
at all times.

They ensure that there are no regressions, act as general documentation for the codebase,
and ensure that the project can evolve over time.

To avoid brittle tests, tests need to validate behavior. A test cannot rely on the internal code structure, so most
mocking should be avoided.

### Test coverage

Our [SonarQube quality gate](https://sonarcloud.io/organizations/refinedmods/quality_gates/show/9) requires a minimum
test coverage percentage of 80%. This an aggregated percentage over all
the API modules, with an exclusion for the platform modules.

> The `refinedstorage2-platform-*` modules are excluded because they contain a lot of Minecraft-specific code and are
> harder to test.

### Mutation testing

We also use [Pitest](https://pitest.org/) mutation testing.

For the API modules, our build pipeline requires a minimum test coverage percentage of 80% and a minimum mutation
coverage percentage of 90%.

## Release process

1) Make sure the version number in `build.gradle` is correct.
2) Merge `develop` to `main` (**with a merge commit**, not by rebasing as rebasing messes up later merges).
3) Push a tag with the version number on the merge commit on `main` (prefixed with `v`).

After releasing:

1) Rename the "Unreleased" section to the correct version number in `CHANGELOG.md`.
2) Upgrade the version number in `build.gradle`.
3) Create a new "Unreleased" section in `CHANGELOG.md`.

## Pipelines

We have a few pipelines:

- Build (PRs, pushes to `develop` and `main`)
- Release (push of a tag)
- Changelog Checker (PRs)
    - To validate if `CHANGELOG.md` is updated. Not every pull request needs a changelog change, so
      the `skip-changelog` label can be added to the pull request to ignore this.
- Commitlint (PRs)
    - Validates whether the commits on a pull request
      respect [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
- Unsupported (issues)
    - Posts a message on a GitHub issue if the issue is about an unsupported version.

### Build

The build pipeline triggers when a pull request is updated or when a commit is pushed to `develop` or `main`.

A Gradle build is ran, running our tests in the process and generating an aggregated code coverage report for the API
modules.

The code is analyzed on SonarQube.

> Because of
> [limitations with SonarQube](https://portal.productboard.com/sonarsource/1-sonarcloud/c/50-sonarcloud-analyzes-external-pull-request),
> pull requests originating from a fork aren't analyzed on SonarQube.

The code style is validated with Checkstyle.

After that, Pitest ensures decent line and mutation coverage.

The artifacts from the build are available on the GitHub Action.

### Release

The release pipeline triggers when a tag is pushed. The tag is validated whether it's
valid [Semver](https://semver.org/spec/v2.0.0.html).

After that, a build is run similar to the build pipeline.

After that succeeds, it will build Javadoc documentation and publish it
on [GitHub pages](https://github.com/refinedmods/refinedstorage2/tree/gh-pages).

The release is published
on [GitHub packages](https://github.com/refinedmods/refinedstorage2/packages), [GitHub releases](https://github.com/refinedmods/refinedstorage2/releases)
and CreeperHost Maven.

The "Unreleased" section in `CHANGELOG.md` is parsed and a GitHub release is created with the changelog body and
relevant artifacts.

After that, a Discord and Twitter announcement is sent.

## Modules

Refined Storage 2 is modularized, the project is split up into various modules.

Most modules aren't dependent on Minecraft or a mod loader. Only modules that start
with `refinedstorage2-platform-*` have dependencies on Minecraft.

| Name                              | Use in addons | Description                                                                           |
|-----------------------------------|---------------|---------------------------------------------------------------------------------------|
| `refinedstorage2-core-api`        | ✔️            | Contains some utility classes and enums.                                              |
| `refinedstorage2-grid-api`        | ✔️            | Contains Grid related functionality.                                                  |
| `refinedstorage2-network-api`     | ✔️            | Contains storage network related functionality.                                       |
| `refinedstorage2-network`         | ❌             | Contains implementations of `refinedstorage2-network-api`.                            |
| `refinedstorage2-network-test`    | ✔️            | JUnit extension which helps with setting up a network and a network node for testing. |
| `refinedstorage2-query-parser`    | ✔️            | A query parser, contains a lexer and parser. Only used for Grid query parsing.        |
| `refinedstorage2-resource-api`    | ✔️            | Contains API for handling resources.                                                  |
| `refinedstorage2-storage-api`     | ✔️            | Contains storage related functionality.                                               |
| `refinedstorage2-platform-api`    | ✔️            | Implements the various Refined Storage API modules for use in Minecraft.              |
| `refinedstorage2-platform-fabric` | ❌             | The platform module for Fabric. This module contains Fabric specific code.            |
| `refinedstorage2-platform-forge`  | ❌             | The platform module for Forge. This module contains Forge specific code.              |
| `refinedstorage2-platform-common` | ❌             | Common mod code. Most gameplay code is in here.                                       |
| `refinedstorage2-platform-test`   | ❌             | This module is used in platform tests for various Minecraft related helpers.          |
