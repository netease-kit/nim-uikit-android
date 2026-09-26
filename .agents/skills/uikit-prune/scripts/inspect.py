#!/usr/bin/env python3
"""Inspect a nim-uikit-android checkout without modifying it."""

import argparse
import json
import re
import subprocess
from pathlib import Path


FEATURE_MARKERS = {
    "message-reaction": [
        "ReactionGroupView",
        "MessageReactionState",
        "ReactionEmojiManager",
        "chat_message_action_emoji_reaction",
        "enableMessageReaction",
    ],
    "earliest-unread": [
        "EarliestUnreadController",
        "earliestUnread",
        "lastReadPosition",
        "chat_message_earliest_unread",
        "enableLastReadPosition",
    ],
}


def files(root):
    ignored = {".git", ".gradle", "build", "out", "generated", "intermediates"}
    return (
        path for path in root.rglob("*")
        if path.is_file() and not any(part in ignored for part in path.relative_to(root).parts)
    )


def find_markers(root, markers):
    matches = []
    for path in files(root):
        if path.suffix not in {".java", ".kt", ".xml", ".gradle", ".kts", ".properties"}:
            continue
        try:
            content = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for marker in markers:
            if marker in content:
                matches.append({"marker": marker, "path": str(path.relative_to(root))})
    return matches


def inspect(root):
    modules = []
    for build_file in root.rglob("build.gradle.kts"):
        module = build_file.parent
        if (module / "src/main/res-normal").is_dir() or (module / "src/main/res-fun").is_dir():
            modules.append({
                "module": str(module.relative_to(root)),
                "normal": (module / "src/main/res-normal").is_dir(),
                "fun": (module / "src/main/res-fun").is_dir(),
                "build_file": str(build_file.relative_to(root)),
            })
    dependencies = []
    for build_file in root.rglob("build.gradle.kts"):
        try:
            content = build_file.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        if re.search(r"\b(api|implementation|compileOnly|runtimeOnly)\s*\(\s*['\"]", content):
            dependencies.append(str(build_file.relative_to(root)))
    return {
        "root": str(root),
        "revision": subprocess.run(["git", "-C", str(root), "rev-parse", "HEAD"], capture_output=True, text=True, check=True).stdout.strip(),
        "worktree_status": subprocess.run(["git", "-C", str(root), "status", "--porcelain"], capture_output=True, text=True, check=True).stdout.splitlines(),
        "skin_modules": sorted(modules, key=lambda item: item["module"]),
        "maven_build_files": sorted(dependencies),
        "features": {
            name: find_markers(root, markers) for name, markers in FEATURE_MARKERS.items()
        },
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path.cwd())
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--status-file", type=Path, help="write the pre-change git status manifest")
    args = parser.parse_args()
    root = args.repo.resolve()
    if not root.is_dir():
        parser.error(f"repository does not exist: {root}")
    result = inspect(root)
    if args.status_file:
        status = subprocess.run(
            ["git", "-C", str(root), "status", "--porcelain"],
            capture_output=True,
            text=True,
            check=True,
        ).stdout
        args.status_file.write_text(status, encoding="utf-8")
    if args.json:
        print(json.dumps(result, indent=2, ensure_ascii=True))
        return
    print(f"repository: {root}")
    print("skin modules:")
    for item in result["skin_modules"]:
        print(f"  {item['module']}: normal={item['normal']} fun={item['fun']}")
    print("feature markers:")
    for name, matches in result["features"].items():
        print(f"  {name}: {'FOUND' if matches else 'NOT_PRESENT'} ({len(matches)} matches)")
    print(f"Maven build files (immutable): {len(result['maven_build_files'])}")


if __name__ == "__main__":
    main()
