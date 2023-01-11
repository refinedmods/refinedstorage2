# 3. The platform API module

Date: 2021-12-29

## Status

Accepted

## Context

Now that we have platform modules (one for each modloader, and a common module) [[1]](#1) and API
modules [[2]](#2), we still need a way for addons to integrate with the mod.

Doing so via the common module would be detrimental as it would expose too much code to addons (just like it did in
Refined Storage 1).

## Decision

Refined Storage 2 will have a modloader-neutral (just like the common module) platform API module which addon mods can
use to integrate with Refined Storage.

Moreover, Refined Storage 2 itself will use this platform API module.

The module is named `refinedstorage2-platform-api`.

## Consequences

By offering a dedicated platform API module we can much more tightly control API surface.

The platform API module is platform-neutral so addons can decide what modloader they target. Moreover, it saves us time
because we don't have to maintain different platform APIs per modloader.

However, if addon mods want to support multiple modloaders, they'll have to create their own abstractions.

## References

- <a id="1">[1]</a> See [1. Multi-loader architecture](001-multi-loader-architecture.md)
- <a id="2">[1]</a> See [2. API modules](002-api-modules.md)
- https://github.com/refinedmods/refinedstorage2/commit/64e97fd170185d3d55a60879db7fca2134ae6dd0