# Skin Pruning

The request is to retain exactly one supported skin in local UIKit source and remove the other skin's source/resources and wiring without breaking shared behavior.

1. Discover every affected module from Gradle settings, module source sets, source package layout, route/UIService registrations, and resource references. Do not assume the skin is confined to `chatkit-ui` or that directory names alone define ownership.
2. Build a keep/remove inventory per module: skin-specific source, `res-*` directories, skin layouts/drawables/strings, route constants and registrations, factories, and references from shared source. Include resource source-set declarations that currently package both variants.
3. Trace references both ways. A class or resource used by both skins is shared and must remain; replace skin-specific callers with the retained implementation only when an established equivalent exists. Never remove shared functionality as a shortcut.
4. Check module consumers and sample/app wiring. For skin pruning, include app/sample routes, UIService registrations, skin-selection branches, imports, and skin-specific resources that directly reference the removed skin. List each path separately and require explicit confirmation. Do not alter app dependencies or unrelated app code. If a source module is not the artifact consumed by the app, say so in the validation report.
5. Verify the retained skin has no references to removed resources/packages, excluded resources are no longer packaged by the affected modules, and every affected library module compiles and passes relevant Lint.

The preview helper removes only skin-owned local source packages and resource source sets. It never deletes app files automatically, but reports app skin references as `REVIEW_REQUIRED`; those references require a version-specific transformer or manual approved edits.

Select one recipe:

- [Keep Normal](../catalog/skins/keep-normal.yaml): remove Fun-owned code and resources.
- [Keep Fun](../catalog/skins/keep-fun.yaml): remove Normal-owned code and resources.
