package io.github.antmordel.kstack.settings

import io.hammerhead.karooext.models.DataType

/** How a field arranges its secondary values beneath the primary one. */
enum class SecondaryLayout {
    /** Two per row. Costs one row instead of two, which is height the primary keeps. */
    SIDE_BY_SIDE,

    /** One per row, in the order the definition lists them. */
    STACKED,
    ;

    companion object {
        val Default = SIDE_BY_SIDE
    }
}

/** Whether a field colours itself by the rider's current zone, and what it colours. */
enum class ZoneColorMode {
    /** No colouring at all. */
    NONE,

    /** The metric icon takes the zone colour; every other pixel is unchanged. */
    ICON,

    /** The field background takes the zone colour, and the text follows the background. */
    FIELD,
    ;

    companion object {
        /**
         * Colouring the icon is the interesting default: it is what a rider gets from the stock
         * heart rate field, and it costs nothing legible — the numbers keep the device's own
         * colours. Off is a choice, not the starting point.
         */
        val Default = ICON
    }
}

/** Which colour palette scheme to use for zone colouring. */
enum class ZonePaletteScheme {
    KAROO,
    GARMIN,
    ZWIFT,
    ;

    companion object {
        val Default = KAROO
    }
}

/** Power smoothing window for primary power display. */
enum class PowerAveraging(
    val dataTypeId: String,
    val badge: String,
    val label: String,
) {
    INSTANT(DataType.Type.POWER, "", "1s"),
    SMOOTHED_3S(DataType.Type.SMOOTHED_3S_AVERAGE_POWER, "3s", "3s"),
    SMOOTHED_5S(DataType.Type.SMOOTHED_5S_AVERAGE_POWER, "5s", "5s"),
    SMOOTHED_10S(DataType.Type.SMOOTHED_10S_AVERAGE_POWER, "10s", "10s"),
    SMOOTHED_30S(DataType.Type.SMOOTHED_30S_AVERAGE_POWER, "30s", "30s"),
    ;

    fun next(): PowerAveraging {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }

    companion object {
        val Default = INSTANT
    }
}

/**
 * The rider's appearance choices for one field.
 *
 * Held per `fieldId` rather than globally, so colouring heart rate does not colour speed.
 */
data class FieldSettings(
    val secondaryLayout: SecondaryLayout = SecondaryLayout.Default,
    val zoneColorMode: ZoneColorMode = ZoneColorMode.Default,
    val zonePaletteScheme: ZonePaletteScheme = ZonePaletteScheme.Default,
    val powerAveraging: PowerAveraging = PowerAveraging.Default,
)

/**
 * Reads settings back from what was stored.
 *
 * A value that is absent or no longer a known option falls back to the default, so a downgrade or
 * a renamed enum entry leaves the rider with a working field rather than a crash.
 */
fun fieldSettingsFrom(
    storedLayout: String?,
    storedZoneColorMode: String?,
    storedPaletteScheme: String? = null,
    storedPowerAveraging: String? = null,
): FieldSettings =
    FieldSettings(
        secondaryLayout = SecondaryLayout.entries.firstOrNull { it.name == storedLayout }
            ?: SecondaryLayout.Default,
        zoneColorMode = ZoneColorMode.entries.firstOrNull { it.name == storedZoneColorMode }
            ?: ZoneColorMode.Default,
        zonePaletteScheme = ZonePaletteScheme.entries.firstOrNull { it.name == storedPaletteScheme }
            ?: ZonePaletteScheme.Default,
        powerAveraging = PowerAveraging.entries.firstOrNull { it.name == storedPowerAveraging }
            ?: PowerAveraging.Default,
    )
