#!/usr/bin/env python3
"""Run non-mutating self-checks for the UIKit pruning skill."""

import argparse
import ast
import json
import subprocess
from pathlib import Path


def run(command, root):
    return subprocess.run(command, cwd=root, capture_output=True, text=True, check=True).stdout


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path.cwd())
    parser.add_argument(
        "--expect-official-baseline",
        action="store_true",
        help="also assert that the official baseline has no local Reaction/earliest-unread implementation",
    )
    args = parser.parse_args()
    root = args.repo.resolve()
    skill = Path(__file__).resolve().parent.parent

    for script in (skill / "scripts").glob("*.py"):
        ast.parse(script.read_text(encoding="utf-8"), filename=str(script))

    inspect = json.loads(
        run(["python3", str(skill / "scripts/inspect.py"), "--repo", str(root), "--json"], root)
    )
    modules = inspect["skin_modules"]
    assert modules, "no dual-skin modules detected"
    assert all(item["normal"] and item["fun"] for item in modules), "incomplete skin detection"
    assert inspect["maven_build_files"], "Maven boundary was not detected"

    if args.expect_official_baseline:
        assert inspect["features"]["message-reaction"] == [], "Reaction unexpectedly matched official baseline"
        assert inspect["features"]["earliest-unread"] == [], "earliest-unread unexpectedly matched official baseline"

    if args.expect_official_baseline:
        reaction = run(
            ["python3", str(skill / "scripts/plan.py"), "remove-message-reaction", "--repo", str(root)], root
        )
        unread = run(
            ["python3", str(skill / "scripts/plan.py"), "remove-earliest-unread", "--repo", str(root)], root
        )
        assert "status: NOT_PRESENT" in reaction and "no edits are allowed" in reaction
        assert "status: NOT_PRESENT" in unread and "no edits are allowed" in unread

    for document in [skill / "SKILL.md", *skill.glob("references/*.md")]:
        assert "TODO" not in document.read_text(encoding="utf-8"), f"unfinished text: {document}"
    feature_mode = "official absent-feature no-op" if args.expect_official_baseline else "feature discovery"
    print(f"PASS: {len(modules)} dual-skin modules, protected Maven boundary, {feature_mode}")


if __name__ == "__main__":
    main()
