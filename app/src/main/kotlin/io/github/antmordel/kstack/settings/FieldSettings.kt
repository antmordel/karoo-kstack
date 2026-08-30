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

/**
 * The rider's appearance choices for one field.
 *
 * Held per `fieldId` rather than globally, so colouring heart rate does not colour speed.
 */
data class FieldSettings(
    val secondaryLayout: SecondaryLayout = SecondaryLayout.Default,
)

/**
 * Reads settings back from what was stored.
 *
 * A value that is absent or no longer a known option falls back to the default, so a downgrade or
 * a renamed enum entry leaves the rider with a working field rather than a crash.
 */
fun fieldSettingsFrom(storedLayout: String?): FieldSettings = FieldSettings(
    secondaryLayout = SecondaryLayout.entries.firstOrNull { it.name == storedLayout }
        ?: SecondaryLayout.Default,
)
