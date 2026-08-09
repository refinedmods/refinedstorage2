---
name: mutation-testing
description: Run mutation testing (PIT/pitest) on a Gradle module and close the gaps it finds. Use when the user says "run mutation testing", asks to check mutation coverage / test strength, or wants to verify that tests actually catch bugs (not just execute lines) after writing or changing code.
---

# Mutation testing (PIT / pitest)

Line coverage says a line *ran*; mutation testing says a bug in that line would be
*caught*. PIT mutates the bytecode (flips conditionals, removes calls, changes return
values, …) and reruns the tests. A mutation that still passes the tests = **SURVIVED** =
a real gap in the assertions. This skill runs PIT, surfaces the survivors, and closes them.

## Workflow

### 1. Determine scope
Figure out which module and which classes matter — you almost always care only about the
code that was just written or changed, **not** the whole module (there are usually
pre-existing survivors elsewhere you should not chase).

- Module: the Gradle project containing the changed files (e.g. `refinedstorage-network`).
- In-scope classes: `git status --short` / `git diff --name-only develop...` on `src/main`.
  Keep the simple class names or package fragments to use as `--filter` values later.

### 2. Run PIT
```
./gradlew :<module>:pitest
```
- Takes ~1 minute for this repo. Run with a generous timeout (e.g. 600000ms) or in the
  background — do not let it look hung.
- It runs the module's whole test suite against generated mutants; a normal run ends with
  `BUILD SUCCESSFUL` even when mutants survive.
- Output: `<module>/build/reports/pitest/mutations.xml` — one `<mutation>` entry per mutant
  with its status, class, method, line, mutator, and description. Read it with the script
  below rather than by eye.

### 3. Extract the survivors
Use the bundled script (parses `mutations.xml`, prints a compact grouped summary, exits 1 if
any in-scope mutation survived):
```
python3 .claude/skills/mutation-testing/scripts/extract_mutations.py <module> --filter <ClassOrPkg>
```
- `<module>` can be the module dir (it finds `build/reports/pitest/mutations.xml`), the
  pitest dir, or the `mutations.xml` file directly.
- Pass `--filter` once per in-scope class/package to ignore unrelated pre-existing gaps.
  Repeat the flag to widen scope: `--filter Foo --filter Bar`.
- `--json` for machine-readable output. `--all` includes KILLED mutations (with the test
  that killed each). `--status SURVIVED` narrows to one status.
- No `--filter` scans the entire module (noisy; use only for a full audit).

### 4. Interpret the statuses
- **KILLED** — good, a test caught the mutation. Goal is all-killed in scope.
- **SURVIVED** — the mutated code ran but no assertion noticed. A real gap: strengthen a test.
- **NO_COVERAGE** — no test exercises that line at all. Add a test that hits it.
- **TIMED_OUT** — usually an infinite-loop mutant that PIT killed via timeout; normally fine.
- **RUN_ERROR / MEMORY_ERROR** — infra noise, not a test-quality signal.

### 5. Kill each survivor
The description tells you exactly what changed; write an assertion that observes that change.
Map the mutation to an observable effect and assert on it:
- `removed call to X::clear` / `X::set` / `X::add` → assert the *post-state* that the call
  produces (e.g. after a reset, assert a getter that reads the cleared collection returns
  empty / null — not just the collections the test already checks).
- `replaced boolean return with false` / `true` → assert both truthy and falsy outcomes.
- `changed conditional boundary` (`<` vs `<=`) → add a test exactly at the boundary value.
- `negated conditional` / `replaced return value` → assert the branch/value the mutation
  would break.

Prefer adding an assertion to an existing test that already exercises the line (the
mutation's method + line number tell you where to look) over writing a whole new test.

### 6. Verify
Re-run the module tests (`./gradlew :<module>:test --tests "*.<Pkg>.*"`) and checkstyle,
then re-run `:<module>:pitest` and the extract script. Confirm the in-scope survivors are
gone (script prints "✅ No surviving/uncovered mutations in scope" and exits 0).

## Notes / gotchas
- **Scope discipline.** The module-wide report will show survivors in code you didn't touch.
  Do not fix those unless asked — report the module score but act only within `--filter`.
- **Equivalent mutants** exist: some mutations can't be killed because they don't change
  observable behavior. If a survivor is genuinely equivalent, say so instead of contorting a
  test to kill it.
- The extract script is pure-stdlib Python 3, no dependencies. `--help` documents all flags.
