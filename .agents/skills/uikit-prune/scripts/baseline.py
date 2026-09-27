#!/usr/bin/env python3
"""Write or inspect a customer-revision baseline for UIKit pruning."""

import argparse
import hashlib
import json
import subprocess
from pathlib import Path


PROTECTED_NAMES = {"gradle.properties", "gradle/libs.versions.toml", "libs.versions.toml"}


def git(root, *args):
    return subprocess.run(["git", "-C", str(root), *args], capture_output=True, text=True, check=True).stdout.strip()


def tracked_files(root):
    output = git(root, "ls-files", "--cached", "--others", "--exclude-standard")
    return [root / line for line in output.splitlines() if line]


def protected(path, root):
    relative = path.relative_to(root).as_posix()
    return (
        path.name in PROTECTED_NAMES
        or path.suffix in {".gradle", ".gradle.kts"}
        or "/libs/" in f"/{relative}"
        or relative.startswith("app/")
    )


def snapshot(root):
    files = []
    for path in tracked_files(root):
        if not path.is_file() or not protected(path, root):
            continue
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        files.append({"path": path.relative_to(root).as_posix(), "sha256": digest})
    return {
        "revision": git(root, "rev-parse", "HEAD"),
        "status": git(root, "status", "--porcelain"),
        "protected_files": sorted(files, key=lambda item: item["path"]),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path.cwd())
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--check", action="store_true", help="compare the current protected-file hashes with a baseline")
    parser.add_argument("--allow-path", action="append", default=[], help="approved path to exclude from protected-content comparison; repeatable")
    args = parser.parse_args()
    root = args.repo.resolve()
    if not root.is_dir():
        parser.error(f"repository does not exist: {root}")
    current = snapshot(root)
    if not args.check:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(current, indent=2, ensure_ascii=True) + "\n", encoding="utf-8")
        print(f"baseline written: {args.output}")
        print(f"revision: {current['revision']}")
        print(f"protected files: {len(current['protected_files'])}")
        return
    if not args.output.is_file():
        parser.error(f"baseline does not exist: {args.output}")
    expected = json.loads(args.output.read_text(encoding="utf-8"))
    expected_files = {item["path"]: item["sha256"] for item in expected.get("protected_files", [])}
    actual_files = {item["path"]: item["sha256"] for item in current["protected_files"]}
    allowed = set(args.allow_path)
    changed = sorted(
        path for path in set(expected_files) | set(actual_files)
        if path not in allowed and expected_files.get(path) != actual_files.get(path)
    )
    if changed:
        print("FAIL: protected content changed")
        for path in changed:
            print(f"- {path}")
        raise SystemExit(1)
    print(f"PASS: protected content unchanged ({len(actual_files)} files)")


if __name__ == "__main__":
    main()
