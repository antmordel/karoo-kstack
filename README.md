# KStack

Garmin-style stacked data fields for [Hammerhead Karoo](https://www.hammerhead.io/), built on the official
[karoo-ext](https://github.com/hammerheadnav/karoo-ext) SDK.

A stock Karoo field shows one value. KStack shows a metric's current value large, with its ride
aggregates small underneath — three numbers in the space of one.

## Fields

| Field | Primary | Below it |
|---|---|---|
| HR Stack | Heart rate | avg, max |
| HR% Stack | Heart rate as % of max HR | avg, max |
| Speed Stack | Speed | avg, max |
| Power Stack | Power | normalized, avg |

Aggregates come from Karoo's own data types, so they match the stock fields exactly and follow the
same pause and reset behaviour. Text scales to whatever grid size the field is given.

## Status

Under construction. Nothing to install yet — the first release will attach an `app-release.apk` here.

## License

Apache-2.0. See [LICENSE](LICENSE).
