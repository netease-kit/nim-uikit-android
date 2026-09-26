# Official Repository Baseline

This baseline was inspected from `netease-kit/nim-uikit-android` revision `d6cef8e9c72873c8f5198bfdbf217d440a52487c` on 2026-09-23. It is evidence for the initial profile only, not a permanent file manifest.

## Confirmed structure

- The repository has local UIKit modules including `chatkit-ui`, `conversationkit-ui`, `localconversationkit-ui`, `teamkit-ui`, and `contactkit-ui`.
- Those modules include both `src/main/res-normal` and `src/main/res-fun`; `chatkit-ui/build.gradle.kts` includes both in its main resource source set.
- `chatkit-ui` has local Android source/resources and Maven dependencies, including `chatkit`, `corekit-plugin`, `common-ui`, and `alog`. Their coordinates and declarations are outside pruning scope.
- The sample app declares Maven UIKit artifacts. An app build may therefore consume published artifacts rather than local source modules; inspect the actual dependency graph before treating it as source validation.

## Feature signature check

At this revision, exact searches in local source found no `EarliestUnreadController`, `MessageReactionState`, `ReactionGroupView`, `chat_message_earliest_unread`, `chat_message_action_emoji_reaction`, `enableLastReadPosition`, or `enableMessageReaction` matches. Therefore the initial feature recipes must return `NOT_PRESENT` unless broader behavioral inspection finds a different local implementation. This absence is not proof that a later revision or differently named implementation is absent.

## Refresh rule

For a later revision, compare module/source-set structure and inspect the relevant behavior again. Do not use the absence result above as a permanent rule, and do not auto-delete based on a changed marker set. Update this document only after checking the new source and validating the resulting recipes.

## Dirty checkout rule

A local customer fork may contain uncommitted UIKit feature work that is absent from this official baseline, including Reaction or earliest-unread code. Treat that worktree as a separate source baseline: record it, do not overwrite or revert it, and do not claim that this official-baseline result applies to it. Generate a new plan only after the customer explicitly identifies that revision as the target.
