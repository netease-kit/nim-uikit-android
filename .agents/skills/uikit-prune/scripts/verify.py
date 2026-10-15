#!/usr/bin/env python3
"""Verify conservative post-prune invariants without changing the checkout."""

import argparse
import subprocess
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path.cwd())
    parser.add_argument("--removed-skin", choices=["normal", "fun"])
    parser.add_argument("--baseline-status", type=Path, help="status manifest created before the operation")
    args = parser.parse_args()
    root = args.repo.resolve()
    failures = []
    if args.removed_skin:
        for path in root.rglob(f"res-{args.removed_skin}"):
            if path.is_dir() and ".git" not in path.parts:
                failures.append(f"removed skin resource directory remains: {path.relative_to(root)}")
    changed = subprocess.run(["git", "-C", str(root), "diff", "--name-only"], capture_output=True, text=True, check=True).stdout.splitlines()
    untracked = subprocess.run(["git", "-C", str(root), "ls-files", "--others", "--exclude-standard"], capture_output=True, text=True, check=True).stdout.splitlines()
    changed = sorted(set(changed) | set(untracked))
    baseline = set()
    if args.baseline_status and args.baseline_status.exists():
        for line in args.baseline_status.read_text(encoding="utf-8").splitlines():
            baseline.add(line[3:] if len(line) >= 3 else line)
    forbidden = [
        path for path in changed
        if path not in baseline
        and (path.endswith((".gradle", ".gradle.kts")) or "/libs/" in path or path.startswith("app/"))
    ]
    if not args.baseline_status and any(path.endswith((".gradle", ".gradle.kts")) or "/libs/" in path or path.startswith("app/") for path in changed):
        print("WARN: pass --baseline-status to distinguish pre-existing protected changes")
    if forbidden:
        failures.append("protected paths changed: " + ", ".join(forbidden))
    if args.baseline_status and args.baseline_status.exists():
        baseline_paths = {line[3:] if len(line) >= 3 else line for line in args.baseline_status.read_text(encoding="utf-8").splitlines()}
        unexpected = sorted(path for path in untracked if path not in baseline_paths and path.startswith(("app/", "libs/")))
        if unexpected:
            failures.append("new protected untracked paths: " + ", ".join(unexpected))
    if failures:
        print("FAIL")
        for failure in failures:
            print(f"- {failure}")
        raise SystemExit(1)
    print("PASS: no checked invariant failed")


if __name__ == "__main__":
    main()
