#!/usr/bin/env python3
"""Create a fresh skin-pruning preview copy; never changes the input checkout."""

import argparse
import re
import shutil
from pathlib import Path


def candidate_paths(root, remove):
    candidates = []
    for module in root.iterdir():
        if not module.is_dir() or not (module / "build.gradle.kts").exists():
            continue
        resource = module / "src/main" / f"res-{remove}"
        if resource.is_dir():
            candidates.append(resource)
        for source_root in (module / "src/main/java", module / "src/main/kotlin"):
            if source_root.is_dir():
                candidates.extend(path for path in source_root.rglob(remove) if path.is_dir())
    return candidates


def remove_resource_source_set(build_file, remove):
    content = build_file.read_text(encoding="utf-8")
    old = content
    content = re.sub(rf',\s*"src/main/res-{re.escape(remove)}"', "", content)
    if content != old:
        build_file.write_text(content, encoding="utf-8")
        return True
    return False


def find_residuals(root, remove):
    tokens = [f"PATH_{remove.upper()}_", f"res-{remove}", f"/{remove}/"]
    residuals = []
    for path in root.rglob("*"):
        if not path.is_file() or ".git" in path.parts or path.suffix not in {".java", ".kt", ".kts", ".xml"}:
            continue
        try:
            content = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        if any(token in content for token in tokens):
            residuals.append(str(path.relative_to(root)))
    return sorted(set(residuals))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, required=True)
    parser.add_argument("--remove", choices=["normal", "fun"], required=True)
    parser.add_argument("--preview-dir", type=Path, required=True)
    args = parser.parse_args()
    source = args.repo.resolve()
    target = args.preview_dir.resolve()
    if not source.is_dir():
        parser.error(f"repository does not exist: {source}")
    if target.exists():
        parser.error(f"preview directory already exists: {target}")
    shutil.copytree(source, target, ignore=shutil.ignore_patterns(".git", "build", ".gradle"))

    removed = []
    changed_build_files = []
    for source_path in candidate_paths(target, args.remove):
        removed.append(str(source_path.relative_to(target)))
        if source_path.is_dir():
            shutil.rmtree(source_path)
    for build_file in target.rglob("build.gradle.kts"):
        if remove_resource_source_set(build_file, args.remove):
            changed_build_files.append(str(build_file.relative_to(target)))
    residuals = find_residuals(target, args.remove)
    print(f"preview: {target}")
    print(f"removed candidates: {len(removed)}")
    for path in sorted(removed):
        print(f"  removed: {path}")
    for path in changed_build_files:
        print(f"  source-set: {path}")
    print(f"residual references: {len(residuals)}")
    for path in residuals:
        print(f"  review: {path}")
    if residuals:
        print("status: REVIEW_REQUIRED")
    else:
        print("status: PREVIEW_CLEAN")


if __name__ == "__main__":
    main()
