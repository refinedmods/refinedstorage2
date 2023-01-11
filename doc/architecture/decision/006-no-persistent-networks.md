# 6. No persistent networks

Date: 2021-05-24

## Status

Accepted

## Context

Networks and network nodes were persisted seperately in Refined Storage 1.

This caused a lot of problems, because there was a disconnect between the real state of the world and the persisted
file.

Most of the time, this didn't lead to any problems, but it quickly failed with block moving mods or mods that worked
with block entities in non-conventional ways.

## Decision

Networks cannot be persisted in Refined Storage 2. They need to be loaded in memory.

Network nodes cannot be persisted in Refined Storage 2. All persistent data should be stored on corresponding the block
entity.

## Consequences

It will no longer be possible to persist global network data, since there are no persistent networks.

This means that some persistent components like resource trackers (when has what been changed in het network?) need to
be stored on the storage itself.

Since networks are no longer persisted and built ad-hoc, it should make the mod more robust. Also, it allows to have
multiple entry-points to a network.

For network nodes, storing everything on the block entity should simplify the code a lot. There is simply no need for
decoupling the storage part, and just causes friction like in Refined Storage 1.

## References

- https://github.com/refinedmods/refinedstorage2/commit/df8f881c1c300e1cb6f02a8ac3f03aa163f634db
