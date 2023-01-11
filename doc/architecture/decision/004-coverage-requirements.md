# 4. Coverage requirements

Date: 2021-12-22

## Status

Accepted

## Context

Since we are writing more unit tests with our API modules [[1]](#1) we need to establish code coverage requirements.

## Decision

We will differentiate between platform modules [[2]](#2) and API modules [[1]](#1). The reason for this is that it's
difficult to test
Minecraft code properly.

Luckily, since our most important (business-logic) code resides in the API modules [[1]](#1) we can put
our testing focus there and establish coverage requirements.

However, sometimes it is still advised to write a test for platform code, even if there are no coverage requirements for
it. For that reason, the `refinedstorage2-platform-test` module exists, to provide helpers to deal with Minecraft code.

## Consequences

For API modules, this means that:

- Minimum 80% code coverage (enforced by
  the [SonarQube quality gate](https://sonarcloud.io/organizations/refinedmods/quality_gates/show/9) and Pitest)
- Minimum 90% mutation coverage (enforced by Pitest)

For platform modules, this means that:

- No code coverage requirement
- No mutation coverage requirement

These requirements will be enforced by SonarQube and Pitest.

## References

- <a id="1">[1]</a> See [2. API modules](002-api-modules.md)
- <a id="2">[2]</a> See [1. Multi-loader architecture](001-multi-loader-architecture.md)
- https://github.com/refinedmods/refinedstorage2/issues/115
