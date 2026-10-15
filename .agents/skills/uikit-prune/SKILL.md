---
name: uikit-prune
description: Remove an unused Normal/Fun skin or a locally implemented UIKit feature from the nim-uikit-android source repository, with dependency-boundary checks and build verification.
---

# UIKit Source Pruning

Use this skill only to remove code/resources from the local UIKit source modules in this repository. It is self-contained: do not expect an internal engineering knowledge base or internal rules repository to exist in the customer's checkout.

## Scope Gate

1. Confirm the repository root, current revision, worktree changes, requested target, and actual module/resource layout. Treat current source and Gradle configuration as authoritative; references in this skill are discovery and safety guidance, not a substitute for checking the checkout.
2. Read [scope-boundary.md](references/scope-boundary.md) before planning any changes. For a skin request, also read [skin-pruning.md](references/skin-pruning.md) and the matching recipe under `catalog/skins/`. For Reaction or earliest-unread removal, read [feature-pruning.md](references/feature-pruning.md) and the matching feature recipe.
3. If the feature or skin is absent, report that it is absent and make no removal edits. If source structure, call chains, shared ownership, or dependencies are ambiguous, stop with the unresolved evidence; do not infer deletions from old file lists.
4. Never modify Maven artifacts, Maven dependency declarations, Gradle dependency versions, generated outputs, caches, or `libs/` binaries. Do not move feature logic into Maven dependencies or edit their sources.
5. This skill is analysis/plan/preview-only until the user explicitly confirms the exact approved file list. Never apply a deletion or source edit based only on a generated plan or a successful preview.

An official clean baseline and a dirty customer fork are different targets. Never use the former's absence/presence result to classify features in the latter; re-run discovery against the explicitly selected target revision.

## Workflow

- Run `python3 scripts/inspect.py --repo <checkout>` first. Use `--json` when another tool needs structured output.
- Run `python3 scripts/baseline.py --repo <checkout> --output <temporary-baseline.json>` before any approved implementation. Use `--check` after implementation to prove protected Gradle/Maven/app content did not change; pass `--allow-path <approved-app-path>` only for individually approved skin-specific app paths.
- Run `python3 scripts/self_test.py --repo <checkout>` to verify the self-contained Skill behavior without changing the checkout. Add `--expect-official-baseline` only for the clean official revision recorded in `official-baseline.md`.
- Use `python3 -c 'import ast, pathlib; [ast.parse(p.read_text()) for p in pathlib.Path("scripts").glob("*.py")]'` for syntax validation in restricted environments; do not rely on `py_compile` when it cannot write its cache directory.
- On a dirty checkout, add `--status-file <path>` and retain that manifest for post-change verification.
- Run `python3 scripts/plan.py <operation> --repo <checkout>` to generate a non-mutating plan. The supported operations are `keep-normal`, `keep-fun`, `remove-message-reaction`, and `remove-earliest-unread`.
- For a skin, run `python3 scripts/preview.py --repo <checkout> --remove fun|normal --preview-dir <new-directory>`. The preview directory must not exist; the script never changes the input checkout.
- Trace every local source and resource reference from entry point through state/model, rendering, callbacks, and registration before proposing deletions. Separate feature-specific code from shared chat behavior.
- Produce a path-by-path plan containing the reason for each edit/deletion, shared code to preserve, Maven dependencies explicitly excluded, and validation tasks. For skin operations, include skin-specific app/sample routes, registrations, selection branches, and resources; keep unrelated app/sample edits separate and obtain explicit confirmation before touching any app path.
- Treat `plan.py --json` as the machine-readable report. It includes the selected revision, worktree-dirty count, immutable paths, candidate edits, blocking reasons, and required checks; `REVIEW_REQUIRED` and `BLOCKED` require human review.
- Preview the change in an isolated copy when practical. Before applying, recheck the target worktree and preserve all pre-existing user edits. Do not overwrite a pre-existing preview or backup directory.
- Apply only the approved plan. Do not broaden the deletion set to make a compile error disappear without tracing the new reference back to its owner.
- Follow [verification.md](references/verification.md). Report source checks, module build/Lint results, app-consumer limitations, and any unverified behavior separately. A successful app build that consumes Maven UIKit artifacts does not prove the local UIKit source changes were compiled.

The inspect and plan scripts do not mutate the repository. The preview script mutates only a newly copied preview directory. A `NOT_PRESENT` plan is a successful no-op, not an error. Do not add a generic auto-delete mode; add a dedicated transformer only after a real source baseline and its validation matrix are established.

There is intentionally no `apply.py` in this skill. After the user confirms a reviewed file list, perform the code change as a separate, explicitly authorized implementation task with a fresh worktree check and the verification matrix.

## Initial Official Baseline

The initial profile was inspected at official revision `d6cef8e9c72873c8f5198bfdbf217d440a52487c`; see [official-baseline.md](references/official-baseline.md). A later revision is not automatically unsupported, but all affected paths and call chains must be rediscovered and the plan revalidated. Update the baseline and recipes only after validating them against the changed source.
