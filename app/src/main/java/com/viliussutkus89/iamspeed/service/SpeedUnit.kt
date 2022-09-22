/*
 * SpeedUnit.kt
 *
 * Copyright (C) 2022 https://www.ViliusSutkus89.com/i-am-speed/
 *
 * I am Speed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.viliussutkus89.iamspeed.service

import android.content.SharedPreferences
import com.viliussutkus89.iamspeed.AppSettings


internal class SpeedUnit(private val sharedPreferences: SharedPreferences) {
    enum class Type {
        KMH, MS, MPH
    }

    private var speedUnit = loadSpeedUnitSetting()

    private fun loadSpeedUnitSetting(): Type {
        return when (AppSettings.get(sharedPreferences, AppSettings.speedUnit)) {
            "kmh" -> Type.KMH
            "mph" -> Type.MPH
            else -> Type.MS
        }
    }

    private val preferencesChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? ->
            if (key == AppSettings.speedUnit) {
                speedUnit = loadSpeedUnitSetting()
            }
        }

    fun start() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    fun stop() {
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
