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
)
