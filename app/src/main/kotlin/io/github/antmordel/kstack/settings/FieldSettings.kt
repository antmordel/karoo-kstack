package io.github.antmordel.kstack.settings

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

/**
 * The rider's appearance choices for one field.
 *
 * Held per `fieldId` rather than globally, so colouring heart rate does not colour speed.
 */
data class FieldSettings(
    val secondaryLayout: SecondaryLayout = SecondaryLayout.Default,
    val zoneColorMode: ZoneColorMode = ZoneColorMode.Default,
)

/**
 * Reads settings back from what was stored.
 *
 * A value that is absent or no longer a known option falls back to the default, so a downgrade or
 * a renamed enum entry leaves the rider with a working field rather than a crash.
 */
fun fieldSettingsFrom(storedLayout: String?, storedZoneColorMode: String?): FieldSettings =
    FieldSettings(
        secondaryLayout = SecondaryLayout.entries.firstOrNull { it.name == storedLayout }
            ?: SecondaryLayout.Default,
        zoneColorMode = ZoneColorMode.entries.firstOrNull { it.name == storedZoneColorMode }
            ?: ZoneColorMode.Default,
    )
