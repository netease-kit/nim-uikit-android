# Verification

Code changes are not part of verification until the user has explicitly approved the exact implementation file list. The default verification target is an isolated preview copy.

The self-check command is non-mutating and validates the Skill's discovery and boundary assumptions. `--expect-official-baseline` is only for the clean official revision; a customer checkout may legitimately contain local feature implementations and should use the default mode.

Derive exact Gradle tasks from the current checkout and modified modules. The initial repository uses a Gradle wrapper; module names and available variants must be rechecked before running commands.

## Static checks

- Search for references to removed class/package names, resource IDs/names, routes, listener methods, payloads, and configuration fields across all local source sets and consumers.
- Verify retained skin resource references resolve and removed skin resource directories are not included in the affected module source sets.
- Review the final diff and assert no Maven coordinate, version, dependency constraint, binary, generated output, or protected app file changed.
- On a dirty checkout, compare protected paths against the status manifest captured by `inspect.py --status-file`; pre-existing paths are not attributed to the pruning operation.
- For stronger protection, create a content baseline with `scripts/baseline.py --output <file>` and run the same command with `--check` after the operation. A changed hash is a failure even when the path was already dirty, because the Skill must not overwrite protected content.
- Include untracked files in the final changed-path review; `git diff` alone does not report newly created source/resources.
- Run `git diff --check`.

## Build and Lint

- Compile each modified UIKit library module and run its applicable Android Lint task.
- Compile direct local source consumers where possible.
- Run the app/sample build only as a separate consumer check. Inspect its dependency graph first: if it consumes Maven UIKit artifacts instead of the modified local project modules, that build does not validate the source changes.
- Do not report a task as passed unless it completed successfully; distinguish dependency-resolution failures from source failures.

## Report

List removed and preserved responsibilities, paths changed, dependency-boundary check, static scan results, exact build/Lint commands and outcomes, and runtime coverage still needed. Build success is not runtime proof.
