# Change Confirmation Gate

The skill may inspect source, generate plans, copy an isolated preview, remove files inside that new preview, and run static checks against the preview. Those actions do not authorize changes to the customer's checkout.

Before any source/resource/build-file edit in the target checkout, present:

- target revision and worktree status;
- the exact files to add, modify, or delete;
- the reason and ownership evidence for every path;
- shared classes/resources that will remain;
- Maven artifacts and dependency declarations explicitly excluded;
- App/sample paths that are blocked or require separate approval;
- planned compile, Lint, resource, and residual-reference checks.

Wait for explicit confirmation of that file list. If the checkout changes after confirmation, stop and regenerate the plan. A user request to design, inspect, preview, or verify does not authorize applying the preview.
