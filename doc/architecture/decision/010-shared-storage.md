# 10. Shared storages

Date: 2024-03-03

## Status

Accepted

## Context

Refined Storage has the concept of a storage channel. A storage channel is the highest-level storage of a network. It
is used by other network devices to interact with the storage network.

As Refined Storage supports multiple resource types, we must decide how we partition those resource types.

## Decision

At the lowest level, we allow mixed resource types within a storage. Since a storage channel is a storage as well, this
means that the highest level, the storage channel, will allow mixed resource types as well.

We don't partition storages by resource type because:

- This implies the use of a generic type on the `Storage` class, which becomes cumbersome quickly when we don't know the
  resource type at runtime (relying on unchecked and rawtypes operations).
- There's no real technical reason to partition storages by resource type. We can have a single storage channel with
  mixed resource types.
- If there is a single storage channel, blocks like the Disk Drive don't need to maintain a storage per storage channel.
  They can expose one storage for all the storage channels, making it easier to reason about.

Implementation provided in [[1]](#1).

## Consequences

- We do remove some compile time safety because we won't have a generic on `Storage`. However:
    1) It's a natural consequence if we want to allow mixed storage channels.
    2) The compile time safety was mostly gone anyway already due to all the unchecked and raw types operations.
- We introduce `ResourceKey` to provide some level of safety (not using `Object`).
- We don't need a storage channel type abstraction (there is only a single storage channel) and move some logic
  to `ResourceKey`.

## References

- <a id="1">[1]</a> See [implementation](https://github.com/refinedmods/refinedstorage2/commit/1fd63d17417e387d427b2e018a93df89e31edc0f)
