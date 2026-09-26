# Repository Agent Guide

## UIKit pruning skill

When a request asks to remove or retain a UIKit skin, remove Message Reaction, or remove the earliest-unread prompt, read:

`.agents/skills/uikit-prune/SKILL.md`

This applies to the local UIKit source modules in this repository. For skin removal, app/sample references that are specific to the removed skin are also analyzed and can be included in the confirmed file list. Maven-provided UIKit artifacts, SDK behavior, `libs/`, generated files, build output, and unrelated app/sample code remain out of scope.

### Usage

1. Inspect the target checkout and current revision.
2. Generate a non-mutating plan with one of:
   - `keep-normal`
   - `keep-fun`
   - `remove-message-reaction`
   - `remove-earliest-unread`
3. Read the relevant references and trace the local call chain before proposing deletions.
4. Present the exact file list, shared code to preserve, immutable paths, and verification commands.
5. Wait for explicit confirmation of that file list before modifying source or resources.
6. Verify the approved change with residual-reference checks, module compile/Lint, and protected-file checks.

Example:

```bash
python3 .agents/skills/uikit-prune/scripts/inspect.py --repo .
python3 .agents/skills/uikit-prune/scripts/plan.py remove-message-reaction --repo . --json
```

Do not infer deletions from old file lists or from a clean official baseline when the customer checkout contains local changes.
