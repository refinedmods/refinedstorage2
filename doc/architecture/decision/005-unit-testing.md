# 5. Unit testing

Date: 2020-10-24

## Status

Accepted

## Context

The coverage requirements [[1]](#1) for the API modules [[2]](#2) state that we need unit test coverage. Hence, we need
to clarify what "unit" in unit test means.

The API modules in Refined Storage 2 are already disconnected from reality (Minecraft), so we need unit tests that
closely mimick real situations and assert real behavior if we want to get any value out of them.

This eliminates the definition that most
people have of unit tests, which is to test "a single method" and to mock dependencies of the class of that method.

## Decision

Refined Storage 2 unit testing will follow a behavior driven approach, where "unit" in unit test means "unit of
behavior" and not "unit of method".

## Consequences

A unit test should verify behavior and not make assumptions about internal code structure, therefore most use of mocking
is not allowed. This will make our test suite stronger and less prone to breakage.

Our tests will not break due to a refactoring.

This doesn't mean that we are suddenly "integration testing", just because we decide to not mock dependencies of
classes. Integration testing will happen in the platform modules [[3]](#3) as Minecraft game tests.

## References

- <a id="1">[1]</a> See [4. Coverage requirements](004-coverage-requirements.md)
- <a id="2">[2]</a> See [2. API modules](002-api-modules.md)
- <a id="3">[3]</a> See [1. Multi-loader architecture](001-multi-loader-architecture.md)