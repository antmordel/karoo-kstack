package io.github.antmordel.kstack.field

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Where a stacked field gets its numbers.
 *
 * karoo-ext exposes streams through callback registration, and its flow adapters live in the SDK's
 * sample rather than the library. Wrapping them behind this interface makes field composition
 * testable without a Karoo or an Android runtime.
 */
interface StreamSource {
    fun stream(dataTypeId: String): Flow<StreamState>

    fun userProfile(): Flow<UserProfile>
}

/** The real source: consumers registered on the Karoo system service. */
class KarooStreamSource(private val karooSystem: KarooSystemService) : StreamSource {

    override fun stream(dataTypeId: String): Flow<StreamState> = callbackFlow {
        val consumerId = karooSystem.addConsumer(OnStreamState.StartStreaming(dataTypeId)) {
                event: OnStreamState ->
            trySendBlocking(event.state)
        }
        awaitClose { karooSystem.removeConsumer(consumerId) }
    }

    override fun userProfile(): Flow<UserProfile> = callbackFlow {
        val consumerId = karooSystem.addConsumer<UserProfile> { trySendBlocking(it) }
        awaitClose { karooSystem.removeConsumer(consumerId) }
    }
}
