package io.github.antmordel.kstack.field

import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val PRIMARY = "primary-type"
private const val AVG = "avg-type"
private const val MAX = "max-type"

private const val LABEL_AVG = 1
private const val LABEL_MAX = 2

private fun streaming(value: Double) =
    StreamState.Streaming(DataPoint("id", mapOf("field" to value)))

private fun profile(maxHr: Int) = UserProfile(
    weight = 70f,
    preferredUnit = UserProfile.PreferredUnit(
        distance = UserProfile.PreferredUnit.UnitType.METRIC,
        elevation = UserProfile.PreferredUnit.UnitType.METRIC,
        temperature = UserProfile.PreferredUnit.UnitType.METRIC,
        weight = UserProfile.PreferredUnit.UnitType.METRIC,
    ),
    maxHr = maxHr,
    restingHr = 50,
    heartRateZones = emptyList(),
    ftp = 250,
    powerZones = emptyList(),
)

private class FakeStreamSource(
    private val streams: Map<String, Flow<StreamState>>,
    private val profiles: Flow<UserProfile>,
) : StreamSource {
    override fun stream(dataTypeId: String) =
        streams[dataTypeId] ?: error("no fake stream for $dataTypeId")

    override fun userProfile() = profiles
}

/** Stand-in for the HR% transform: pure, and declines when the profile cannot support it. */
private val PercentOfMaxHr = ValueTransform { raw, userProfile ->
    userProfile?.maxHr?.takeIf { it > 0 }?.let { raw / it * 100.0 }
}

private val definition = StackedFieldDefinition(
    fieldId = "test-stack",
    primary = StackedValue(PRIMARY),
    secondaries = listOf(
        StackedValue(AVG, LABEL_AVG),
        StackedValue(MAX, LABEL_MAX),
    ),
    iconRes = 3,
)

class StackedFieldStatesTest {

    @Test
    fun `emits before any stream has produced`() = runTest {
        val source = FakeStreamSource(
            streams = mapOf(
                PRIMARY to MutableSharedFlow(),
                AVG to MutableSharedFlow(),
                MAX to MutableSharedFlow(),
            ),
            profiles = MutableSharedFlow(),
        )

        val state = source.stackedFieldStates(definition).first()

        assertNull(state.primary)
        assertEquals(listOf(null, null), state.secondaries.map { it.value })
    }

    @Test
    fun `a row with data does not wait for the others`() = runTest {
        val primary = MutableSharedFlow<StreamState>(replay = 1)
        primary.emit(streaming(142.0))
        val source = FakeStreamSource(
            streams = mapOf(
                PRIMARY to primary,
                AVG to MutableSharedFlow(),
                MAX to MutableSharedFlow(),
            ),
            profiles = MutableSharedFlow(),
        )

        val state = source.stackedFieldStates(definition).first { it.primary != null }

        assertEquals(142.0, state.primary!!, 0.0)
        assertEquals(listOf(null, null), state.secondaries.map { it.value })
    }

    @Test
    fun `an unavailable stream clears only its own row`() = runTest {
        val avg = MutableSharedFlow<StreamState>(replay = 1)
        avg.emit(StreamState.NotAvailable)
        val primary = MutableSharedFlow<StreamState>(replay = 1)
        primary.emit(streaming(142.0))
        val max = MutableSharedFlow<StreamState>(replay = 1)
        max.emit(streaming(191.0))
        val source = FakeStreamSource(
            streams = mapOf(PRIMARY to primary, AVG to avg, MAX to max),
            profiles = MutableSharedFlow(),
        )

        val state = source.stackedFieldStates(definition)
            .first { it.primary != null && it.secondaries[1].value != null }

        assertEquals(142.0, state.primary!!, 0.0)
        assertNull(state.secondaries[0].value)
        assertEquals(191.0, state.secondaries[1].value!!, 0.0)
    }

    @Test
    fun `secondary labels survive composition in order`() = runTest {
        val source = FakeStreamSource(
            streams = mapOf(
                PRIMARY to MutableSharedFlow(),
                AVG to MutableSharedFlow(),
                MAX to MutableSharedFlow(),
            ),
            profiles = MutableSharedFlow(),
        )

        val state = source.stackedFieldStates(definition).first()

        assertEquals(listOf(LABEL_AVG, LABEL_MAX), state.secondaries.map { it.labelRes })
    }

    @Test
    fun `a transform needing the profile yields nothing until it arrives`() = runTest {
        val primary = MutableSharedFlow<StreamState>(replay = 1)
        primary.emit(streaming(95.0))
        // A row that never needs the profile, used as the marker that composition has settled.
        val avg = MutableSharedFlow<StreamState>(replay = 1)
        avg.emit(streaming(88.0))
        val profiles = MutableSharedFlow<UserProfile>(replay = 1)
        val source = FakeStreamSource(
            streams = mapOf(PRIMARY to primary, AVG to avg, MAX to MutableSharedFlow()),
            profiles = profiles,
        )
        val states = source.stackedFieldStates(
            definition.copy(primary = StackedValue(PRIMARY, transform = PercentOfMaxHr)),
        )

        val beforeProfile = states.first { it.secondaries[0].value != null }
        assertNull(beforeProfile.primary)

        profiles.emit(profile(maxHr = 190))
        val afterProfile = states.first { it.primary != null }
        assertEquals(50.0, afterProfile.primary!!, 0.001)
    }

    @Test
    fun `a profile with no max heart rate leaves the row empty`() = runTest {
        val primary = MutableSharedFlow<StreamState>(replay = 1)
        primary.emit(streaming(95.0))
        val avg = MutableSharedFlow<StreamState>(replay = 1)
        avg.emit(streaming(88.0))
        val profiles = MutableSharedFlow<UserProfile>(replay = 1)
        profiles.emit(profile(maxHr = 0))
        val source = FakeStreamSource(
            streams = mapOf(PRIMARY to primary, AVG to avg, MAX to MutableSharedFlow()),
            profiles = profiles,
        )

        val state = source.stackedFieldStates(
            definition.copy(primary = StackedValue(PRIMARY, transform = PercentOfMaxHr)),
        ).first { it.secondaries[0].value != null }

        assertNull(state.primary)
    }
}
