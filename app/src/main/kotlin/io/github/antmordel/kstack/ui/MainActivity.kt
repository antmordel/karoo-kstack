package io.github.antmordel.kstack.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import io.github.antmordel.kstack.R
import io.github.antmordel.kstack.field.Definitions
import io.github.antmordel.kstack.field.StackedFieldDefinition
import io.github.antmordel.kstack.settings.PowerAveraging
import io.github.antmordel.kstack.settings.SecondaryLayout
import io.github.antmordel.kstack.settings.SettingsStore
import io.github.antmordel.kstack.settings.SharedPreferencesSettingsStore
import io.github.antmordel.kstack.settings.ZoneColorMode
import io.github.antmordel.kstack.settings.ZonePaletteScheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * KStack's entry in the Karoo main menu, and its settings screen.
 *
 * Builds one section per entry in [Definitions.all]. Nothing here knows which metric a section is
 * for: the name comes from the definition, and so will every setting added later.
 */
class MainActivity : Activity() {

    private lateinit var settings: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SharedPreferencesSettingsStore(this)
        setContentView(R.layout.activity_main)

        val container = findViewById<ViewGroup>(R.id.fields)
        Definitions.all.forEach { definition ->
            container.addView(fieldSection(definition, container))
        }

        findViewById<Button>(R.id.close).setOnClickListener { finish() }
    }

    private fun fieldSection(definition: StackedFieldDefinition, parent: ViewGroup): View {
        val section = LayoutInflater.from(this).inflate(R.layout.settings_field, parent, false)
        section.findViewById<TextView>(R.id.field_name).setText(definition.nameRes)

        // One read of a SharedPreferences-backed flow on an already-loaded file. The screen has no
        // other state to restore, so an async load would only flash the wrong radio button first.
        val current = runBlocking { settings.settings(definition.fieldId).first() }

        val layoutChoice = section.findViewById<RadioGroup>(R.id.layout_choice)
        layoutChoice.check(current.secondaryLayout.radioId())
        layoutChoice.setOnCheckedChangeListener { _, checkedId ->
            settings.setSecondaryLayout(definition.fieldId, checkedId.toLayout())
        }

        // Offering zone colouring for a metric that has no zones would be a control that does
        // nothing. The definition is what says whether there are any.
        val zoneSection = section.findViewById<View>(R.id.zone_color_section)
        if (definition.zone == null) {
            zoneSection.visibility = View.GONE
        } else {
            val zoneChoice = section.findViewById<RadioGroup>(R.id.zone_color_choice)
            zoneChoice.check(current.zoneColorMode.radioId())
            zoneChoice.setOnCheckedChangeListener { _, checkedId ->
                settings.setZoneColorMode(definition.fieldId, checkedId.toZoneColorMode())
            }

            val paletteChoice = section.findViewById<RadioGroup>(R.id.zone_palette_choice)
            paletteChoice.check(current.zonePaletteScheme.radioId())
            paletteChoice.setOnCheckedChangeListener { _, checkedId ->
                settings.setZonePaletteScheme(definition.fieldId, checkedId.toZonePaletteScheme())
            }
        }

        val powerSection = section.findViewById<View>(R.id.power_averaging_section)
        if (definition.fieldId != Definitions.Power.fieldId) {
            powerSection.visibility = View.GONE
        } else {
            val powerChoice = section.findViewById<RadioGroup>(R.id.power_averaging_choice)
            powerChoice.check(current.powerAveraging.radioId())
            powerChoice.setOnCheckedChangeListener { _, checkedId ->
                settings.setPowerAveraging(definition.fieldId, checkedId.toPowerAveraging())
            }
        }
        return section
    }

    private fun SecondaryLayout.radioId() = when (this) {
        SecondaryLayout.SIDE_BY_SIDE -> R.id.layout_side_by_side
        SecondaryLayout.STACKED -> R.id.layout_stacked
    }

    private fun Int.toLayout() = when (this) {
        R.id.layout_stacked -> SecondaryLayout.STACKED
        else -> SecondaryLayout.SIDE_BY_SIDE
    }

    private fun ZoneColorMode.radioId() = when (this) {
        ZoneColorMode.NONE -> R.id.zone_color_none
        ZoneColorMode.ICON -> R.id.zone_color_icon
        ZoneColorMode.FIELD -> R.id.zone_color_field
    }

    private fun Int.toZoneColorMode() = when (this) {
        R.id.zone_color_icon -> ZoneColorMode.ICON
        R.id.zone_color_field -> ZoneColorMode.FIELD
        else -> ZoneColorMode.NONE
    }

    private fun ZonePaletteScheme.radioId() = when (this) {
        ZonePaletteScheme.KAROO -> R.id.palette_karoo
        ZonePaletteScheme.GARMIN -> R.id.palette_garmin
        ZonePaletteScheme.ZWIFT -> R.id.palette_zwift
    }

    private fun Int.toZonePaletteScheme() = when (this) {
        R.id.palette_garmin -> ZonePaletteScheme.GARMIN
        R.id.palette_zwift -> ZonePaletteScheme.ZWIFT
        else -> ZonePaletteScheme.KAROO
    }

    private fun PowerAveraging.radioId() = when (this) {
        PowerAveraging.INSTANT -> R.id.power_avg_instant
        PowerAveraging.SMOOTHED_3S -> R.id.power_avg_3s
        PowerAveraging.SMOOTHED_5S -> R.id.power_avg_5s
        PowerAveraging.SMOOTHED_10S -> R.id.power_avg_10s
        PowerAveraging.SMOOTHED_30S -> R.id.power_avg_30s
    }

    private fun Int.toPowerAveraging() = when (this) {
        R.id.power_avg_3s -> PowerAveraging.SMOOTHED_3S
        R.id.power_avg_5s -> PowerAveraging.SMOOTHED_5S
        R.id.power_avg_10s -> PowerAveraging.SMOOTHED_10S
        R.id.power_avg_30s -> PowerAveraging.SMOOTHED_30S
        else -> PowerAveraging.INSTANT
    }
}
