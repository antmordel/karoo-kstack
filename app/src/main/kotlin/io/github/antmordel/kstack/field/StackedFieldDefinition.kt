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
    val formatter: ValueFormatter = ValueFormatter.Plain,
) {
    /**
     * What the field shows in the data page editor. Karoo streams nothing there, and a rider
     * arranging a page needs to see the shape of the field rather than a column of dashes.
     */
    fun previewState() = StackedFieldState(
        primary = primary.previewValue,
        secondaries = secondaries.map { SecondaryState(it.labelRes, it.previewValue) },
    )
}
