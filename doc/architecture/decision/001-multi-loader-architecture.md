# 1. Multi-loader architecture

Date: 2021-12-21

## Status

Accepted

## Context

A big reason for rewriting Refined Storage is to have support for multiple modloaders. Therefore, the architecture
should support this design goal in a maintainable way.

## Decision

Refined Storage 2 will have a modular structure with a module per modloader.

There will be a common module, and most code should reside in there. The modloader specific (platform) modules should be
reserved
for platform specific functionality, like registration, networking, etc.

The module naming follows the standard of `refinedstorage2-platform-{name}`.

## Consequences

Thanks to the modular structure, it will be easier to separate modloader specific code and Minecraft code.

Apart from the structural benefits, it will be easier to maintain support for different modloaders as opposed to working
with multiple Git branches and rebasing (or copying code over).

The build system will become more complicated, as the common module needs to be "loaded" into the specific modloader (
platform)
modules.

It will also be harder to update the mod to a newer Minecraft version if not all our supported modloaders are upgraded
yet. However, in that case we can probably disable the specific platform module in the build system.

## References

- https://github.com/refinedmods/refinedstorage2/issues/64