# Scope Boundary

## In scope

- Source, resources, layouts, generated-resource declarations, and UI registrations owned by the local UIKit library modules.
- Skin-specific source and resources under variants such as `normal`, `fun`, `res-normal`, and `res-fun`, only after confirming their role in the current module.
- Feature-specific UIKit entry points, view state, rendering, callbacks, page wiring, layouts, resources, and configuration when the full local call chain confirms they belong to the requested feature.

## Out of scope

- Maven artifacts and all declarations that resolve external artifacts (`api`, `implementation`, version catalogs, plugin-managed coordinates, and dependency constraints). Do not edit, exclude, replace, or inspect a Maven artifact's source to remove a UIKit feature.
- `libs/` JAR/AAR files, Gradle caches, generated code/resources, build outputs, and SDK-owned behavior.
- Customer application behavior outside the selected UIKit modules. For a skin-pruning operation only, app/sample files that directly select, route to, register, or provide resources for the removed skin are reviewable candidates and must be listed individually for confirmation. All unrelated app/sample files remain protected.
- Generic messaging behavior such as message models, conversation unread/read APIs, shared emoji input, or shared message rendering merely because a target feature uses it.

## Dependency classification

Classify code by ownership before editing:

- `project(:module)` and source under that module: local source candidate; inspect its callers and consumers.
- External `group:artifact:version`, version-catalog entry, binary, or Maven-resolved class: immutable dependency boundary.
- App/sample call site: consumer integration. It is in scope only when the skin operation proves the path is specific to the removed skin; feature-pruning operations do not inherit this exception.

If removal from local UIKit would require a change across an immutable dependency boundary, stop and describe the dependency limitation. Do not claim the behavior is removed from the complete application when only its local UI integration was removed.

## Worktree protection

Record status before planning. Never discard, reset, stash, or overwrite user changes. Limit application to reviewed paths, compare each target with the preview immediately before writing, and report paths that changed since planning instead of overwriting them.
