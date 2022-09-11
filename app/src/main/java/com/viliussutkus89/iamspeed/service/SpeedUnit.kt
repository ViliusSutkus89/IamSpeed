package com.viliussutkus89.iamspeed.service

import android.content.SharedPreferences
import com.viliussutkus89.iamspeed.ui.SettingsFragment
import java.io.Closeable


internal class SpeedUnit(private val sharedPreferences: SharedPreferences): Closeable {
    enum class Type {
        KMH, MS, MPH
    }

    private var speedUnit: Type = getSpeedUnitSetting()

    private fun getSpeedUnitSetting(): Type {
        return when (sharedPreferences.getString(SettingsFragment.speedUnit, "kmh")) {
            "kmh" -> Type.KMH
            "mph" -> Type.MPH
            else -> Type.MS
        }
    }

    private val preferencesChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? ->
            if (key == SettingsFragment.speedUnit) {
                speedUnit = getSpeedUnitSetting()
            }
        }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun close() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    fun translate(speed: Float, accuracyMetersPerSecond: Float?): SpeedEntry {
        val speedInt: Int
        val speedStr: String
        val accuracy: String?

        when (speedUnit) {
            Type.MS -> {
                speedInt = speed.toInt()
                speedStr = "$speedInt m/s"
                accuracy = accuracyMetersPerSecond?.toString()
            }
            Type.KMH -> {
                speedInt = (speed * 3.6f).toInt()
                speedStr = "$speedInt km/h"
                accuracy = accuracyMetersPerSecond?.let { (it * 3.6f).toString() }
            }
            Type.MPH -> {
                speedInt = (speed * 2.2369f).toInt()
                speedStr = "$speedInt mph"
                accuracy = accuracyMetersPerSecond?.let { (it * 2.2369f).toString() }
            }
        }

        return SpeedEntry(
            speedInt = speedInt,
            speedStr = speedStr,
            accuracy = accuracy
        )
    }
}
