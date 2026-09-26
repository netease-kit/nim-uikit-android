# Feature Pruning

Feature recipes describe the behavior to discover, not a fixed deletion list. Search the current checkout, trace the complete local call chain, and build a fresh ownership graph before editing. A matching class name alone is insufficient evidence.

## Message Reaction

The requested scope is the UIKit message-reaction experience: entry points, per-message state/rendering, callbacks, popups, UI resources, and feature-only configuration. Preserve generic emoji input, shared message models/rendering, unrelated quick-comment APIs, and all Maven dependencies unless current source proves a local, feature-exclusive wrapper can be removed without crossing the dependency boundary.

Discovery hints from the source profile that informed this skill include `ReactionGroupView`, `MessageReactionState`, `ReactionEmojiManager`, reaction callbacks/payloads, and `chat_message_action_emoji_reaction`. These are search hints only. The initial official baseline did not contain these signatures; see the recipe and [official-baseline.md](official-baseline.md). Do not create deletions from these hints when the implementation is absent.

## Earliest-unread prompt

The requested scope is the chat-page earliest-unread prompt and its dedicated positioning flow. Preserve normal conversation unread counts, read-time APIs, clear-unread behavior, generic history loading, newest-message navigation, and shared message-list state unless a dedicated local branch can be proven exclusive to the prompt.

Discovery hints include `EarliestUnreadController`, `earliestUnread`/`lastReadPosition` state and callbacks, prompt layout IDs, prompt strings, and a global enable flag. They are not a complete manifest. Trace entry lifecycle, count query, boundary detection, anchor lookup, multi-select behavior, and both skin implementations before pruning.

## Required outcome

For either feature, distinguish three outcomes:

- `FOUND_AND_PRUNED`: complete local feature ownership was traced, edits applied, and checks passed.
- `NOT_PRESENT`: no local feature implementation exists; no source edits made.
- `BLOCKED`: implementation is partial, shared, ambiguous, or crosses the Maven boundary; no unapproved edits made.

Do not report `FOUND_AND_PRUNED` when only UI visibility was disabled or when Maven-provided behavior remains in scope.
