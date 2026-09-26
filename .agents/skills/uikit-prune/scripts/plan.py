#!/usr/bin/env python3
"""Generate a conservative, non-mutating UIKit pruning plan."""

import argparse
import importlib.util
import json
import re
from pathlib import Path


_inspect_path = Path(__file__).with_name("inspect.py")
_inspect_spec = importlib.util.spec_from_file_location("uikit_prune_inspect", _inspect_path)
_inspect_module = importlib.util.module_from_spec(_inspect_spec)
_inspect_spec.loader.exec_module(_inspect_module)
inspect_repo = _inspect_module.inspect


def skin_candidates(root, remove):
    candidates = []
    resource_dir = f"res-{remove}"
    for module in root.iterdir():
        if not module.is_dir() or not (module / "build.gradle.kts").exists():
            continue
        resource_path = module / "src/main" / resource_dir
        if resource_path.is_dir():
            candidates.append({"action": "remove-in-preview", "path": str(resource_path.relative_to(root)), "reason": f"{remove} skin resource source set"})
        for source_root in (module / "src/main/java", module / "src/main/kotlin"):
            if not source_root.is_dir():
                continue
            for path in source_root.rglob(remove):
                if path.is_dir():
                    candidates.append({"action": "review-before-removal", "path": str(path.relative_to(root)), "reason": f"{remove} named source package; verify ownership and registrations"})
    return candidates


def app_skin_candidates(root, remove):
    """Find app paths that directly mention the skin being removed; never edit them here."""
    patterns = {
        "fun": re.compile(r"\bFun[A-Z]\w*|\bfun_[a-z0-9_]+|\bfun[A-Z]\w*"),
        "normal": re.compile(r"\bNormal[A-Z]\w*|\bnormal_[a-z0-9_]+|\bnormal[A-Z]\w*"),
    }
    pattern = patterns[remove]
    candidates = []
    app = root / "app"
    if not app.is_dir():
        return candidates
    for path in app.rglob("*"):
        if not path.is_file() or any(part in {"build", "generated", ".gradle"} for part in path.parts):
            continue
        if path.suffix not in {".java", ".kt", ".xml", ".gradle", ".kts"}:
            continue
        try:
            content = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        if pattern.search(content):
            candidates.append({
                "action": "review-before-removal",
                "path": str(path.relative_to(root)),
                "reason": f"app path references removed {remove} skin; verify it is skin-exclusive",
            })
    return candidates


def make_plan(root, operation):
    report = inspect_repo(root)
    plan = {
        "operation": operation,
        "status": "REVIEW_REQUIRED",
        "revision": report["revision"],
        "worktree_status_count": len(report["worktree_status"]),
        "edits": [],
        "blocking": [],
        "immutable": report["maven_build_files"],
        "verification": ["git diff --check", "search residual references", "compile and lint affected local modules"],
    }
    if operation in {"keep-normal", "keep-fun"}:
        remove = "fun" if operation == "keep-normal" else "normal"
        plan["edits"] = skin_candidates(root, remove) + app_skin_candidates(root, remove)
        if not plan["edits"]:
            plan["status"] = "NOT_PRESENT"
            plan["blocking"].append(f"No local {remove} skin source/resource candidate found")
        else:
            plan["blocking"].append("Source package candidates require reverse-reference and shared-ownership review")
    elif operation in {"remove-message-reaction", "remove-earliest-unread"}:
        feature = operation.removeprefix("remove-")
        if not report["features"][feature]:
            plan["status"] = "NOT_PRESENT"
            plan["blocking"].append("No local feature marker found; no edits are allowed")
        else:
            plan["blocking"].append("Feature markers found; complete local call-chain ownership review before editing")
            plan["edits"] = report["features"][feature]
    else:
        raise ValueError(f"unsupported operation: {operation}")
    return plan


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("operation", choices=["keep-normal", "keep-fun", "remove-message-reaction", "remove-earliest-unread"])
    parser.add_argument("--repo", type=Path, default=Path.cwd())
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()
    plan = make_plan(args.repo.resolve(), args.operation)
    if args.json:
        print(json.dumps(plan, indent=2, ensure_ascii=True))
    else:
        print(f"status: {plan['status']}")
        print(f"operation: {plan['operation']}")
        for item in plan["edits"]:
            print(f"{item['action']}: {item['path']} ({item['reason']})")
        for item in plan["blocking"]:
            print(f"BLOCKING: {item}")
        print("Maven files are immutable and are not edit candidates.")


if __name__ == "__main__":
    main()
