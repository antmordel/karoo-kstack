package io.github.antmordel.kstack.field

import androidx.annotation.StringRes

/** One secondary row: its label, and its value or `null` when the stream has nothing to show. */
data class SecondaryState(
    @StringRes val labelRes: Int?,
    val value: Double?,
)

/**
 * Everything a stacked field needs to draw itself, with values transformed but not yet formatted.
 *
 * Rows are independent: any of them may be `null` while the others carry data.
 */
data class StackedFieldState(
    val primary: Double?,
    val secondaries: List<SecondaryState>,
    /**
     * The rider's current zone, as Karoo reports it, or `null` when the definition names no zone
     * stream, the stream has nothing to say, or the rider has configured no zones.
     */
    val zone: Int? = null,
    /** How many zones the rider has configured for this metric. Zero when none are. */
    val zoneCount: Int = 0,
)
