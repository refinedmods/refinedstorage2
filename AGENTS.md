# AGENTS.md

## Code style

### Javadoc

Only files in modules whose name ends in `-api` need Javadoc, as those are consumed by addons.

Don't add Javadoc to classes, methods or fields in the other modules (`refinedstorage-network`,
`refinedstorage-common`, `refinedstorage-fabric`, `refinedstorage-neoforge`, ...). Use a regular
comment there when the *why* isn't obvious from the code, and let the code speak for itself
otherwise.
