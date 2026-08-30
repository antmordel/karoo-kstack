package io.github.antmordel.kstack.field

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.hammerhead.karooext.models.UserProfile
import kotlin.math.roundToInt

/**
 * Turns a raw stream value into the number a row displays.
 *
 * Must be pure: a function of the emission and the profile, with no memory between calls. That is
 * what keeps the extension free of ride state, so averages and maxima stay Karoo's to define.
 *
 * The profile is nullable because it arrives asynchronously and most rows never need it. Returning
 * `null` marks the value missing, which the field renders as a dash.
 */
fun interface ValueTransform {
    fun apply(raw: Double, profile: UserProfile?): Double?

    companion object {
        /** Passes the stream value straight through. The common case. */
        val Identity = ValueTransform { raw, _ -> raw }
    }
}

/**
 * One number in a stacked field: which Karoo stream it reads, how to label it, and how to derive
 * the displayed value from the raw emission.
 *
 * @property dataTypeId a [io.hammerhead.karooext.models.DataType.Type] constant.
 * @property labelRes short label drawn beside the value; `null` for the primary, which has none.
 * @property previewValue plausible reading shown while the rider is editing a data page, where no
 * sensor is streaming. Already in display terms, so no transform is applied to it.
 */
data class StackedValue(
    val dataTypeId: String,
    @StringRes val labelRes: Int? = null,
    val transform: ValueTransform = ValueTransform.Identity,
    val previewValue: Double = 0.0,
)

/**
 * Turns a displayed value into its printed text.
 *
 * Lives on the definition so unit handling stays out of the renderer: a field that carries units
 * converts here, and one that does not uses [Plain].
 */
fun interface ValueFormatter {
    fun format(value: Double, profile: UserProfile?): String

    companion object {
        /** Whole numbers, which is what every unitless metric wants. */
        val Plain = ValueFormatter { value, _ -> value.roundToInt().toString() }
    }
}

/**
 * A complete stacked field: one large primary value over an ordered list of smaller labeled ones.
 *
 * The list is open-ended by design — a field may carry one secondary or four, and adding "last lap
 * average" later is an entry here rather than a change to any renderer.
 *
 * @property fieldId must match a `DataType typeId` in `res/xml/extension_info.xml`.
 */
data class StackedFieldDefinition(
    val fieldId: String,
    /** The name the field picker shows. The settings screen labels its rows with the same string. */
    @StringRes val nameRes: Int,
    val primary: StackedValue,
    val secondaries: List<StackedValue>,
    @DrawableRes val iconRes: Int,
    /**
     * The Karoo stream reporting the rider's current zone for this metric, and the profile zones
     * to size it against. `null` for a metric with no zones, which renders exactly as it does
     * with colouring switched off.
     */
    val zone: ZoneSource? = null,
    val formatter: ValueFormatter = ValueFormatter.Plain,
    /**
     * Drawn small after every value, for a metric whose number does not read on its own. A
     * percentage needs its sign; a speed does not need `km/h`, which Karoo's own fields omit too.
     */
    @StringRes val suffixRes: Int? = null,
) {
    /**
     * Fills any row the streams have nothing for with its preview value, at [step] of the sweep.
     *
     * The page editor does feed the real data types, the way it feeds Karoo's own fields, so a
     * preview shows live numbers where there are any. The fallback is what keeps a field from
     * previewing as a row of dashes when a sensor is not paired.
     */
    fun withPreviewFallback(state: StackedFieldState, step: Int = 0): StackedFieldState {
        val preview = previewState(step)
        return StackedFieldState(
            primary = state.primary ?: preview.primary,
            secondaries = state.secondaries.mapIndexed { index, secondary ->
                secondary.takeIf { it.value != null } ?: preview.secondaries[index]
            },
            zone = state.zone ?: preview.zone,
            zoneCount = state.zoneCount.takeIf { it > 0 } ?: preview.zoneCount,
        )
    }

    /**
     * The stand-in reading at [step] of the sweep.
     *
     * A field frozen on one number reads as broken beside Karoo's own, which move even while the
     * bike is on the stand. Only the primary climbs: an average and a maximum barely shift second
     * to second on a real ride, and one moving number is what a rider is looking for.
     */
    fun previewState(step: Int = 0): StackedFieldState {
        val phase = step.mod(PREVIEW_STEPS).toDouble() / PREVIEW_STEPS
        return StackedFieldState(
            primary = primary.previewValue * (1.0 + PREVIEW_SWEEP * phase),
            secondaries = secondaries.map { SecondaryState(it.labelRes, it.previewValue) },
            // The zone climbs with the value, so a rider previewing a coloured field sees the
            // whole palette rather than one colour that could be the uncoloured default.
            zone = zone?.let { (phase * PREVIEW_ZONE_COUNT).toInt().coerceAtMost(PREVIEW_ZONE_COUNT - 1) },
            zoneCount = zone?.let { PREVIEW_ZONE_COUNT } ?: 0,
        )
    }

    private companion object {
        /**
         * How far the primary climbs across a sweep, as a fraction of its own preview value.
         *
         * Relative rather than absolute because one number has to stay plausible for a heart rate,
         * a speed with a decimal and a lap time counted in seconds.
         */
        const val PREVIEW_SWEEP = 0.07

        /** Steps in one sweep. At the editor's one-second tick, a sweep takes twenty seconds. */
        const val PREVIEW_STEPS = 20

        const val PREVIEW_ZONE_COUNT = 5
    }
}

/**
 * Where a field's zone colouring comes from: the Karoo stream carrying the current zone, and the
 * list on the rider's profile that says how many zones there are.
 *
 * Both are named by the definition, so power zones work without the renderer learning what a zone
 * is — the same rule the value streams follow.
 */
data class ZoneSource(
    val dataTypeId: String,
    val zonesOf: (UserProfile) -> List<UserProfile.Zone>,
)
