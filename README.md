# KStack

[![CI](https://img.shields.io/github/actions/workflow/status/antmordel/karoo-kstack/ci.yml?branch=main)](https://github.com/antmordel/karoo-kstack/actions/workflows/ci.yml)
[![Downloads](https://img.shields.io/github/downloads/antmordel/karoo-kstack/app-release.apk)](https://github.com/antmordel/karoo-kstack/releases)
[![License](https://img.shields.io/github/license/antmordel/karoo-kstack)](LICENSE)

Garmin-style stacked data fields for [Hammerhead Karoo](https://www.hammerhead.io/), built on the
official [karoo-ext](https://github.com/hammerheadnav/karoo-ext) SDK.

A stock Karoo field shows one value. KStack shows a metric's current value large, with its ride
aggregates small underneath — three numbers in the space of one.

Compatible with Karoo 2 and Karoo 3.

![Six KStack fields on a Karoo data page](fields.png)

## Fields

| Field | Large | Underneath |
|---|---|---|
| HR Stack | Heart rate | avg, max |
| HR% Stack | Heart rate as a percentage of your max HR | avg, max |
| Speed Stack | Speed | avg, max |
| Power Stack | Power | NP, avg |
| Cadence Stack | Cadence | avg, max |
| Time Stack | Lap time | total elapsed, stops included |

All six are graphical fields and scale their text to whatever grid size you place them in.

The aggregates are Karoo's own `AVERAGE_*` and `MAX_*` data types, so they match the stock fields
exactly and follow the same pause and reset behaviour. HR% divides those same heart rate values by
the max HR in your Karoo user profile; if you have not set one, those rows stay empty.

Speed is shown in km/h or mph according to the unit preference in your Karoo profile. Rows resolve
independently: a sensor that is not connected shows `--` on its own row while the others keep
updating.

## Zone colouring and settings

Open KStack from the main menu to reach its settings. Each field has its own:

- **Zone colour** — off, the metric icon in the colour of your current zone, or the whole field
  background in it with the text following. Heart rate and power have zones; speed, cadence and
  time do not, so those fields offer no colour choice.
- **Secondary values** — side by side, or stacked one per row.

Changes apply to fields already on a data page, without re-adding them.

## Installation

Download `app-release.apk` from the [latest release](https://github.com/antmordel/karoo-kstack/releases/latest).

**Karoo 3** — use [Hammerhead's companion app sideloading](https://support.hammerhead.io/hc/en-us/articles/31576497036827-Companion-App-Sideloading):
open the release page in your phone's browser, long-press the `app-release.apk` link and share it
with the Hammerhead Companion app, then press Install on the Karoo.

**Karoo 2** — enable sideloading ([DC Rainmaker's guide](https://www.dcrainmaker.com/2021/02/how-to-sideload-android-apps-on-your-hammerhead-karoo-1-karoo-2.html))
and run `adb install app-release.apk`.

To update later, long-press the KStack icon in the main menu and select Update.

## Usage

Open KStack once from the main menu after installing. The four fields then appear in the data field
picker when you edit a data page, listed under their names above.

## Building from source

`karoo-ext` is published only to GitHub Packages, which requires authentication even though the
package is public. Create a personal access token with the `read:packages` scope and put it in
`local.properties`:

```properties
gpr.user=your-github-username
gpr.key=ghp_your_token
```

`GPR_USER` and `GPR_KEY` environment variables work too. Then:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

`./gradlew assembleRelease` produces an unsigned APK unless the signing environment variables are
set; see [AGENTS.md](AGENTS.md) for how releases are built and signed.

## Credits

- Built on [karoo-ext](https://github.com/hammerheadnav/karoo-ext) (Apache-2.0)
- Project structure and release conventions follow [timklge/karoo-headwind](https://github.com/timklge/karoo-headwind)

## License

Apache-2.0. See [LICENSE](LICENSE).
