package io.github.antmordel.kstack.field

import io.github.antmordel.kstack.R
import io.hammerhead.karooext.models.DataType

/**
 * The stacked fields KStack publishes.
 *
 * Every entry here needs a matching `DataType` element in `res/xml/extension_info.xml`, or it
 * never reaches the field picker.
 */
object Definitions {

    val HeartRate = StackedFieldDefinition(
        fieldId = "hr-stack",
        primary = StackedValue(DataType.Type.HEART_RATE),
        secondaries = listOf(
            StackedValue(DataType.Type.AVERAGE_HR, R.string.label_avg),
            StackedValue(DataType.Type.MAX_HR, R.string.label_max),
        ),
        iconRes = R.drawable.ic_kstack,
    )

    val all = listOf(HeartRate)
}
