package io.github.antmordel.kstack.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Reads and writes the rider's per-field appearance choices. */
interface SettingsStore {
    /** Emits the current settings for [fieldId] immediately, then again on every change. */
    fun settings(fieldId: String): Flow<FieldSettings>

    fun setSecondaryLayout(fieldId: String, layout: SecondaryLayout)

    fun setZoneColorMode(fieldId: String, mode: ZoneColorMode)
}

/**
 * Backed by `SharedPreferences`.
 *
 * A change listener only fires in the process that wrote the value, which is fine here because the
 * settings screen and the extension service share one process — neither declares `android:process`.
 * Splitting them would silently stop placed fields from reacting to a setting change.
 *
 * Keys are `<fieldId>.<setting>`, so a field definition added later needs no migration.
 */
class SharedPreferencesSettingsStore(context: Context) : SettingsStore {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun settings(fieldId: String): Flow<FieldSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySendBlocking(read(fieldId))
        }
        trySendBlocking(read(fieldId))
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override fun setSecondaryLayout(fieldId: String, layout: SecondaryLayout) {
        preferences.edit().putString(layoutKey(fieldId), layout.name).apply()
    }

    override fun setZoneColorMode(fieldId: String, mode: ZoneColorMode) {
        preferences.edit().putString(zoneColorKey(fieldId), mode.name).apply()
    }

    private fun read(fieldId: String) = fieldSettingsFrom(
        storedLayout = preferences.getString(layoutKey(fieldId), null),
        storedZoneColorMode = preferences.getString(zoneColorKey(fieldId), null),
    )

    private fun layoutKey(fieldId: String) = "$fieldId.secondaryLayout"

    private fun zoneColorKey(fieldId: String) = "$fieldId.zoneColorMode"

    private companion object {
        const val FILE_NAME = "kstack-settings"
    }
}
