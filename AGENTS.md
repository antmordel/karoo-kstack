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
- **AGP 9 builds Kotlin itself.** The `org.jetbrains.kotlin.android` plugin must not be applied, and
  `kotlinOptions` is gone — the JVM target lives in a top-level `kotlin { compilerOptions { } }`
  block. Java 11 is the floor.
- **The SDK 37 platform package is `platforms;android-37.0`, not `android-37`.** Installing the name
  that looks right leaves the build without a platform. Build tools are `build-tools;37.0.0`, and
  `release.yml` names both once in job `env` because the signature check reaches into build-tools by
  path — when that drifted, the failure only showed up after a tag had been pushed.

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

The runner image (`myoung34/github-runner`) carries **no JDK, no Android SDK, and no `gh` CLI**.
Workflows install the toolchain per job with `setup-java` and `setup-android`; anything needing the
GitHub API needs an action rather than a `gh` script step.

## Releasing

Pushing a `v*` tag runs `.github/workflows/release.yml`, which builds a signed `app-release.apk` and
attaches it, `manifest.json` and `kstack.png` to the GitHub release.

- `versionName` comes from the tag with the `v` stripped; `versionCode` from `github.run_number`, so
  it only ever increases. Android refuses an in-place update otherwise.
- Signing reads `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` from the
  environment. With none of them set the release build is unsigned, so a local `assembleRelease`
  still works — it just produces `app-release-unsigned.apk`, which will not install.
- The keystore lives in the `KEYSTORE_BASE64` repo secret, is decoded into `RUNNER_TEMP`, and is
  removed in an `always()` step because the runner host is long-lived.
- `manifest.json` is generated, never committed, so the version it advertises cannot drift from the
  APK beside it. The task declares its version inputs; without them Gradle skips it as UP-TO-DATE
  and republishes the previous version's numbers.
- Losing the keystore means no user can ever update an installed KStack in place.

## Zone colouring

`HR_ZONE` and `POWER_ZONE` are native streams, so nothing derives a zone from thresholds. A
definition names its zone stream and the profile list to size it against; a definition that names
none renders exactly as it does with colouring off, which is what Speed Stack does.

- **karoo-ext publishes no palette.** `UserProfile.Zone` is `(min, max)` and nothing else. The
  colours in `render/ZoneColors.kt` are Karoo's own, as its Heart Rate Zones and Power Zones
  settings screens draw them, so a Karoo OS change can drift from them.
- **There are two scales, not one stretched to fit.** Heart rate has five zones and power seven;
  they share their first four colours and diverge above threshold. A definition names its
  `ZonePalette`, and `ZonePalette.zoneCount` is also what a coloured field previews across. A rider
  with more zones than Karoo defines saturates at the top colour rather than losing it.
- **On a zone-coloured field the text colour comes from the background, not the device.** Amber
  needs black text in night mode too. The choice uses the WCAG contrast ratio rather than a
  brightness threshold, which picks wrongly for the saturated mid stops, and `ContrastTest` holds
  every palette entry to the 3:1 large-text threshold — so a new colour cannot be added blind.
- **`zoneIndex` assumes Karoo reports Z1 as 1**, matching how its own screens label zones. It is a
  one-line change if a device says otherwise, and `ZonesTest` pins the behaviour either way.

## Settings are per field, and definition-driven

Appearance choices are stored per `fieldId` and applied by the renderer, which receives them the
same way it receives the definition. The settings screen builds itself from `Definitions.all`, so a
field added later appears there with no UI change.

Persistence is `SharedPreferences`. Its change listener only fires in the process that wrote the
value, which works because the settings screen and the extension service share one process —
neither declares `android:process`. Splitting them would silently stop a placed field from
reacting to a setting change.

## Non-goals

No companion app. No graphs (sk0711-graph covers it). No unit suffix beside a value — Karoo's own
speed fields show none either. Which secondary rows a field shows is not rider-selectable; the
settings screen could host it, but the catalog of selectable rows per metric is undecided. Do not
add these speculatively.
