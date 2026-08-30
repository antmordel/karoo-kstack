package io.github.antmordel.kstack.field

import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.math.roundToInt

/**
 * Composes the streams a [definition] names into the state its field renders.
 *
 * Every stream is seeded with `null` so the field draws as soon as any one of them produces,
 * rather than staying blank until all of them have. A row whose stream is unavailable, or whose
 * transform declines the value, stays `null` on its own without affecting the others.
 */
fun StreamSource.stackedFieldStates(
    definition: StackedFieldDefinition,
): Flow<StackedFieldState> {
    val values = listOf(definition.primary) + definition.secondaries

    val rawValues = combine(values.map { value -> stream(value.dataTypeId).seededValues() }) {
        it.toList()
    }
    val profiles = userProfile().map<UserProfile, UserProfile?> { it }.onStart { emit(null) }
    // A definition with no zone source still needs a flow to combine, or the field would only
    // draw once the other streams line up with something that never arrives.
    val zones = definition.zone
        ?.let { stream(it.dataTypeId).seededValues() }
        ?: flowOf(null)

    return combine(rawValues, profiles, zones) { raws, profile, reportedZone ->
        val transformed = values.mapIndexed { index, value ->
            raws[index]?.let { value.transform.apply(it, profile) }
        }
        val zoneCount = definition.zone
            ?.let { source -> profile?.let { source.zonesOf(it).size } }
            ?: 0
        StackedFieldState(
            primary = transformed.first(),
            secondaries = definition.secondaries.mapIndexed { index, secondary ->
                SecondaryState(secondary.labelRes, transformed[index + 1])
            },
            zone = reportedZone?.let { zoneIndex(it.roundToInt(), zoneCount) },
            zoneCount = zoneCount,
        )
    }.distinctUntilChanged()
}

private fun Flow<StreamState>.seededValues(): Flow<Double?> =
    map { it.value() }.onStart { emit(null) }

/**
 * Reads the number out of a stream emission.
 *
 * `singleValue` takes the first entry of the data point's value map, which is unambiguous only for
 * single-field data types. Every type KStack reads is single-field; a multi-field type would need
 * the definition to name which field it wants.
 */
private fun StreamState.value(): Double? =
    (this as? StreamState.Streaming)?.dataPoint?.singleValue
