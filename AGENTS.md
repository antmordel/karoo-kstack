# KStack — working notes for agents

Garmin-style stacked data fields for Hammerhead Karoo, on the official
[karoo-ext](https://github.com/hammerheadnav/karoo-ext) SDK. Kotlin/Android, Apache-2.0.

## The one architectural rule

**No metric-specific code outside a field definition.** One renderer draws every stacked field.
Adding power, cadence, or a "last lap avg" row must be a new `StackedFieldDefinition` or a new
entry in an existing definition's secondary list — never a new renderer, view, or subclass. If you
find yourself writing `if (dataType == HEART_RATE)` anywhere in `render/`, stop.

A field is: one primary value, plus an ordered, **open-ended** list of labeled secondary values.
Never assume exactly two secondaries.

## The extension holds no ride state

Every value is a native Karoo data type (`AVERAGE_HR`, `MAX_SPEED`, `NORMALIZED_POWER`, …) or a
**pure** transform of one against `UserProfile`. Nothing accumulates across a ride.

This is deliberate and load-bearing: it is why KStack's averages match the stock Karoo fields
exactly, and why pause, resume, ride restart and lap reset need no handling here. Karoo exposes a
single `AVERAGE_*` per metric with no moving/elapsed variant — that is the value KStack shows. Do
not "fix" this by computing a moving average; it would reintroduce ride state and make the fields
disagree with the rest of the device.

HR% is the shape to imitate: all three rows are HR data types divided by `UserProfile.maxHr`. Pure
division, no state, and it gets a max row that Karoo does not publish natively.

## Facts that bite

- **karoo-ext resolves only from GitHub Packages, and always needs auth** — even though the package
  is public. Set `gpr.user` / `gpr.key` in `local.properties`, or `GPR_USER` / `GPR_KEY` in the
  environment. Secrets and env vars cannot be named `GITHUB_*` — GitHub reserves that prefix. A
  clean clone does not build without credentials.
- **CI cannot use the automatic `GITHUB_TOKEN`** for that: it is scoped to this repo and cannot read
  another org's packages. A PAT with `read:packages` must exist as a repo secret.
- **`KarooExtension.types` and `res/xml/extension_info.xml` must agree.** A definition missing from
  the XML never reaches the field picker, and nothing fails loudly when they drift.
- **Extension id cannot contain a `.`** — the SDK asserts this at construction.
- **Text scaling comes from `ViewConfig`** (`textSize`, `gridSize`, `viewSize`), never from measuring
  a laid-out view. Honour `ViewConfig.alignment`, and render placeholders when `preview` is true.
- **Rows resolve independently.** A stream that is unavailable or a transform returning `null`
  renders `--` for that row alone. `combine` must emit on partial data — seed each flow with `null`
  rather than waiting for every stream to produce once, or the field stays blank.
- **`assembleRelease` is unsigned by default.** Release signing reads a base64 keystore from the
  environment; see the release workflow.
- Karoo 2 is the `minSdk` floor. Do not raise it without checking Karoo 2.

## Conventions

- `oxlint`-equivalent for this stack: `./gradlew lint` with `warningsAsErrors = true`. Zero warnings
  is the baseline — fix them or add an inline suppression with a justification.
- Absolute imports; no wildcard imports.
- Comments explain *why*. If a comment explains *what*, refactor instead.
- Test behavior, not implementation. Stream composition is unit-testable with fake flows — cover
  partial data, dropped streams, and a profile with no max HR.
- Commits: imperative mood, ≤72 char subject, one logical change. No attribution trailers.
- Never push to `main`; branch and open a PR.
- Pin GitHub Actions to full SHAs with a version comment, set `persist-credentials: false`, and run
  `actionlint` and `zizmor` before merging a workflow change.

## CI runs on self-hosted runners

Runners are defined outside this repo, in `antmordel/ballix-infra` at
`metis-vps/github-runner/docker-compose.yml`. Every runner there shares the label set
`self-hosted,linux,x64` — isolation comes from `RUNNER_SCOPE=repo`, not from labels, so workflows
use `runs-on: [self-hosted, linux, x64]` and nothing more specific.

The runner image carries **no JDK and no Android SDK**. Build jobs run in a container with the SDK,
using the docker socket already mounted on the runner. Do not install a toolchain onto the VPS.

## Non-goals

No companion app. No settings UI — secondary rows are fixed per definition. No zone coloring
(BigNum covers it). No graphs (sk0711-graph covers it). Do not add these speculatively.
