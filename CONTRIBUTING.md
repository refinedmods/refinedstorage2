# Contributing to Refined Storage

## Pull requests

- Keep your PR as small as possible, this makes reviewing easier.
- Commits serve a clear purpose and have a fitting commit message.
- Branches are kept up to date by rebasing, preferably.
- Changes are added in `CHANGELOG.md`. Please refrain from using technical terminology, keep it user-friendly.

## Branching

Regarding naming, please use `feature/`, followed by the ID of the issue on the issue tracker, followed by a short
description. For example: `feature/1783/disk-drive`.

For bugfixes, use `fix/`as a prefix.

## Documentation

If you are adding functionality or changing behavior, please update the Patchouli documentation.

## Code style

Please use the `.editorconfig` file as provided.

## Testing

Changes to the `core` module need unit tests.

Changes to platform modules like `fabric` don't necessarily need these, but they are welcome.
