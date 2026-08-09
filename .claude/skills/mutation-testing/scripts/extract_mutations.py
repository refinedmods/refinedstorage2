#!/usr/bin/env python3
"""Extract surviving (and other non-killed) mutations from a PIT/pitest report.

Reads PIT's `mutations.xml` and prints a compact, grouped summary of every mutation a test
did NOT kill, so the gaps are easy to act on.

Usage:
    python3 extract_mutations.py <path> [options]

    <path>   Path to `mutations.xml`, or a pitest report dir containing it, or a Gradle
             module dir (its build/reports/pitest/mutations.xml is used).
             e.g. refinedstorage-network

Options:
    --filter SUBSTR   Only include mutations whose mutated class (or source file) contains
                      SUBSTR (repeatable). Use this to focus on the code you changed and
                      ignore pre-existing survivors elsewhere.
    --status S        Only include mutations with this status (repeatable).
                      Default: everything that is not KILLED.
    --all             Include KILLED mutations too (implies no status filter).
    --json            Emit machine-readable JSON instead of the text summary.

Exit code: 1 if any in-scope non-killed mutations were found, else 0.
"""
import argparse
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

# Statuses PIT assigns. KILLED is the only "good" one; the rest are gaps or noise.
GOOD = "KILLED"
NOISE = {"TIMED_OUT"}  # usually equivalent/infra, not a real test gap — flagged but de-emphasized


def find_report(path):
    """Resolve <path> to a mutations.xml file (accepts the file, a pitest dir, or a module dir)."""
    candidates = [
        path,
        os.path.join(path, "mutations.xml"),
        os.path.join(path, "build", "reports", "pitest", "mutations.xml"),
    ]
    for c in candidates:
        if os.path.isfile(c):
            return c
    return None


def _readable_test(raw):
    """Turn PIT's verbose JUnit test id into 'TestClass.method(...)'.

    e.g. 'com...FooTest.[engine:junit-jupiter]/[class:...]/[method:shouldBar(x.Y)]'
         -> 'FooTest.shouldBar(x.Y)'
    """
    if not raw:
        return ""
    cls = raw.split(".[engine", 1)[0]
    simple = cls.rsplit(".", 1)[-1] if "." in cls else cls
    method = re.search(r"\[method:([^\]]+)\]", raw)
    return f"{simple}.{method.group(1)}" if method else (simple or raw)


def parse(xml_path):
    root = ET.parse(xml_path).getroot()
    out = []
    for mut in root.findall("mutation"):
        line = mut.findtext("lineNumber")
        mutator = mut.findtext("mutator", "")
        out.append({
            "class": mut.findtext("mutatedClass", ""),
            "source_file": mut.findtext("sourceFile", ""),
            "line": int(line) if line and line.isdigit() else None,
            "status": mut.get("status", ""),
            "detected": mut.get("detected") == "true",
            "method": mut.findtext("mutatedMethod", ""),
            "mutator": mutator.rsplit(".", 1)[-1] if mutator else "",
            "killed_by": _readable_test(mut.findtext("killingTest") or ""),
            "description": mut.findtext("description", ""),
        })
    return out


def main(argv):
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("path")
    ap.add_argument("--filter", action="append", default=[], metavar="SUBSTR")
    ap.add_argument("--status", action="append", default=[], metavar="STATUS")
    ap.add_argument("--all", action="store_true")
    ap.add_argument("--json", action="store_true")
    args = ap.parse_args(argv)

    xml_path = find_report(args.path)
    if not xml_path:
        print(f"error: no mutations.xml found at/under '{args.path}'", file=sys.stderr)
        print("       run the pitest task first (e.g. ./gradlew :<module>:pitest)", file=sys.stderr)
        return 2

    mutations = parse(xml_path)

    def in_scope(mut):
        if args.filter and not any(
            f in mut["class"] or f in mut["source_file"] for f in args.filter
        ):
            return False
        if args.all:
            return True
        if args.status:
            return mut["status"] in args.status
        return mut["status"] != GOOD

    issues = [m for m in mutations if in_scope(m)]

    if args.json:
        print(json.dumps(issues, indent=2))
        return 1 if issues else 0

    total = len(mutations)
    scanned_classes = sorted({m["class"] for m in mutations})
    print(f"Scanned {total} mutations across {len(scanned_classes)} class(es) in {xml_path}")
    if args.filter:
        print(f"Filter: {', '.join(args.filter)}")

    if not issues:
        print("\n✅ No surviving/uncovered mutations in scope. Test suite killed everything it should.")
        return 0

    by_class = {}
    for m in issues:
        by_class.setdefault(m["class"], []).append(m)

    print(f"\n⚠️  {len(issues)} mutation(s) not killed:\n")
    for cls in sorted(by_class):
        muts = sorted(by_class[cls], key=lambda x: (x["line"] or 0))
        print(f"  {cls}  ({len(muts)})")
        for m in muts:
            tag = f"  [{m['status']}]" if m["status"] not in NOISE else f"  [{m['status']} — often equivalent]"
            print(f"    L{m['line']:<4} {m['description']}{tag}")
            print(f"           in {m['method']}()  via {m['mutator']}")
        print()

    counts = {}
    for m in issues:
        counts[m["status"]] = counts.get(m["status"], 0) + 1
    print("Summary: " + ", ".join(f"{k}={v}" for k, v in sorted(counts.items())))
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
