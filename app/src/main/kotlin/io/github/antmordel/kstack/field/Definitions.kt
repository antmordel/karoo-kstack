package io.github.antmordel.kstack.field

import io.github.antmordel.kstack.R
import io.hammerhead.karooext.models.DataType

/**
 * The stacked fields KStack publishes.
 *
 * Every entry needs a matching `DataType` element in `res/xml/extension_info.xml`, or it never
 * reaches the field picker. `DefinitionsTest` holds the two to that.
 *
 * Adding a metric is an entry here. Adding a row to an existing metric is an entry in its
 * [StackedFieldDefinition.secondaries]. Neither reaches the renderer.
 */
object Definitions {

    /**
     * Percent of the rider's configured maximum.
     *
     * Karoo publishes `PERCENT_MAX_HR` as a current value but no average or maximum for it, so all
     * three rows are derived from the heart rate types instead. Pure division, so the field still
     * holds no ride state. A profile with no max heart rate yields nothing, which renders as a dash
     * rather than a zero or a divide error.
     */
    private val PercentOfMaxHr = ValueTransform { raw, profile ->
        profile?.maxHr?.takeIf { it > 0 }?.let { raw / it * 100.0 }
    }

    val HeartRate = StackedFieldDefinition(
        fieldId = "hr-stack",
        zone = ZoneSource(DataType.Type.HR_ZONE, ZonePalette.HEART_RATE) { it.heartRateZones },
        nameRes = R.string.field_hr_stack,
        primary = StackedValue(DataType.Type.HEART_RATE, previewValue = 142.0),
        secondaries = listOf(
            StackedValue(DataType.Type.AVERAGE_HR, R.string.label_avg, previewValue = 128.0),
            StackedValue(DataType.Type.MAX_HR, R.string.label_max, previewValue = 176.0),
        ),
        iconRes = R.drawable.ic_heart,
    )

    val HeartRatePercent = StackedFieldDefinition(
        fieldId = "hr-percent-stack",
        zone = ZoneSource(DataType.Type.HR_ZONE, ZonePalette.HEART_RATE) { it.heartRateZones },
        nameRes = R.string.field_hr_percent_stack,
        primary = StackedValue(
            DataType.Type.HEART_RATE,
            transform = PercentOfMaxHr,
            previewValue = 75.0,
        ),
        secondaries = listOf(
            StackedValue(
                DataType.Type.AVERAGE_HR,
                R.string.label_avg,
                transform = PercentOfMaxHr,
                previewValue = 67.0,
            ),
            StackedValue(
                DataType.Type.MAX_HR,
                R.string.label_max,
                transform = PercentOfMaxHr,
                previewValue = 93.0,
            ),
        ),
        iconRes = R.drawable.ic_heart,
        suffixRes = R.string.suffix_percent,
    )

    val Speed = StackedFieldDefinition(
        fieldId = "speed-stack",
        nameRes = R.string.field_speed_stack,
        primary = StackedValue(DataType.Type.SPEED, previewValue = 8.9),
        secondaries = listOf(
            StackedValue(DataType.Type.AVERAGE_SPEED, R.string.label_avg, previewValue = 7.5),
            StackedValue(DataType.Type.MAX_SPEED, R.string.label_max, previewValue = 15.0),
        ),
        iconRes = R.drawable.ic_speed,
        formatter = speedFormatter(),
    )

    val Power = StackedFieldDefinition(
        fieldId = "power-stack",
        zone = ZoneSource(DataType.Type.POWER_ZONE, ZonePalette.POWER) { it.powerZones },
        nameRes = R.string.field_power_stack,
        primary = StackedValue(DataType.Type.POWER, previewValue = 248.0),
        secondaries = listOf(
            StackedValue(DataType.Type.NORMALIZED_POWER, R.string.label_norm, previewValue = 264.0),
            StackedValue(DataType.Type.AVERAGE_POWER, R.string.label_avg, previewValue = 231.0),
        ),
        iconRes = R.drawable.ic_power,
    )

    val Cadence = StackedFieldDefinition(
        fieldId = "cadence-stack",
        nameRes = R.string.field_cadence_stack,
        primary = StackedValue(DataType.Type.CADENCE, previewValue = 88.0),
        secondaries = listOf(
            StackedValue(DataType.Type.AVERAGE_CADENCE, R.string.label_avg, previewValue = 84.0),
            StackedValue(DataType.Type.MAX_CADENCE, R.string.label_max, previewValue = 112.0),
        ),
        iconRes = R.drawable.ic_cadence,
    )

    /**
     * Lap time with the ride's total elapsed time beneath it.
     *
     * Moving time and the time of day are deliberately absent: Karoo already draws both in the
     * status bar above every ride page, so a field spending rows on them would be duplication.
     */
    val Time = StackedFieldDefinition(
        fieldId = "time-stack",
        nameRes = R.string.field_time_stack,
        primary = StackedValue(DataType.Type.ELAPSED_TIME_LAP, previewValue = 754.0),
        secondaries = listOf(
            StackedValue(DataType.Type.ELAPSED_TIME, R.string.label_total, previewValue = 5310.0),
        ),
        iconRes = R.drawable.ic_time,
        formatter = durationFormatter(),
    )

    val all = listOf(HeartRate, HeartRatePercent, Speed, Power, Cadence, Time)
}
