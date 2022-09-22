/*
 * IamSpeedApplication.kt
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

package com.viliussutkus89.iamspeed

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager


class IamSpeedApplication: MultiDexApplication() {
    private val sharedPreferences: SharedPreferences? get() = PreferenceManager.getDefaultSharedPreferences(this)

    override fun onCreate() {
        super.onCreate()
        sharedPreferences.let {
            it?.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
            updateDayNightMode(it)
        }
    }

    private val preferencesChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            if (key == AppSettings.lightDark) {
                updateDayNightMode(sharedPreferences)
            }
        }

    private fun updateDayNightMode(sharedPreferences: SharedPreferences?) {
        val dayNightMode = when (AppSettings.get(sharedPreferences, AppSettings.lightDark)) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(dayNightMode)
    }
}
