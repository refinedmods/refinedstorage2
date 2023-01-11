# 2. API modules

Date: 2021-08-21

## Status

Accepted

## Context

Most of the business logic in Refined Storage 1 is interweaved with Minecraft specific code.

This makes it harder to write unit tests and to maintain the codebase.

Along with the negative impact on testability, this makes our business logic prone to changes in Minecraft. Ideally, our
business logic shouldn't change due to a Minecraft update.

## Decision

Refined Storage 2 will have multiple API modules that have no dependency on Minecraft whatsoever.
These modules can depend on each other, but the dependency chain should be logical.

These API modules can be consumed by platform modules (to bundle them) [[1]](#1) and addon developers.

The platform modules [[1]](#1) are responsible for integrating the modules with Minecraft, by implementing interfaces on
their end that the API modules can use.

The API modules have the name `refinedstorage2-{name}-api`.

## Consequences

Using multiple API modules ties in nicely with our modular structure for platform modules [[1]](#1).

It will be difficult to integrate these modules with Minecraft code, so designing them should be done thoughtfully.

It will be easier to maintain and unit test these modules as they have no dependency on Minecraft.

## References

- <a id="1">[1]</a> See [1. Multi-loader architecture](001-multi-loader-architecture.md)
- https://github.com/refinedmods/refinedstorage2/commit/42cd440dfa227673be88917193a5327285d85f47
- https://github.com/refinedmods/refinedstorage2/commit/a3fce1c12bc5e1d06db4441a78b9edc56852f2bc
- https://github.com/refinedmods/refinedstorage2/commit/d46fba9bed46edaa3c7ed57fc780b10d724c4a2c
- https://github.com/refinedmods/refinedstorage2/commit/5b798047e9573e787e071e14fe7de122218389f9
- https://github.com/refinedmods/refinedstorage2/commit/66416a25f0c32127c94aed265c5e62a2b4374f31
- https://github.com/refinedmods/refinedstorage2/commit/bcf3324b71ea837fb044b051b8d5bb9c9e108028
- https://github.com/refinedmods/refinedstorage2/commit/1b86e4364561160ceda9a62f6f5e10298d989791
