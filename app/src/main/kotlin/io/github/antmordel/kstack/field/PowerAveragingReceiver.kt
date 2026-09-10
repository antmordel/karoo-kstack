package io.github.antmordel.kstack.field

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import io.github.antmordel.kstack.settings.PowerAveraging
import io.github.antmordel.kstack.settings.SharedPreferencesSettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Handles tap broadcasts sent from the power field view.
 *
 * Detecting double-tap within [DOUBLE_TAP_TIMEOUT_MS] cycles power averaging through
 * Instant (1s), 3s, 5s, 10s, 30s, matching Karoo's stock behaviour.
 */
class PowerAveragingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fieldId = intent.getStringExtra(EXTRA_FIELD_ID) ?: Definitions.Power.fieldId
        val now = SystemClock.uptimeMillis()
        val lastTap = lastTapTimeByField[fieldId] ?: 0L
        val diff = now - lastTap
        lastTapTimeByField[fieldId] = now

        Timber.d("Power field tapped for %s, diff=%d ms", fieldId, diff)

        if (diff in MIN_TAP_INTERVAL_MS..DOUBLE_TAP_TIMEOUT_MS) {
            // Valid double-tap detected; reset to avoid a subsequent 3rd tap chaining
            lastTapTimeByField[fieldId] = 0L

            val store = SharedPreferencesSettingsStore(context)
            val currentSettings = runBlocking { store.settings(fieldId).first() }
            val nextAveraging = currentSettings.powerAveraging.next()

            Timber.i("Double-tap detected on %s: cycling power averaging from %s to %s",
                fieldId, currentSettings.powerAveraging, nextAveraging)
            store.setPowerAveraging(fieldId, nextAveraging)
        }
    }

    companion object {
        const val ACTION_CYCLE_POWER = "io.github.antmordel.kstack.ACTION_CYCLE_POWER"
        const val EXTRA_FIELD_ID = "fieldId"

        private const val MIN_TAP_INTERVAL_MS = 40L
        private const val DOUBLE_TAP_TIMEOUT_MS = 650L

        private val lastTapTimeByField = mutableMapOf<String, Long>()
    }
}
