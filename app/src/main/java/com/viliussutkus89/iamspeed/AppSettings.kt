/*
 * AppSettings.kt
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


class AppSettings {
    companion object {
        const val lightDark = "lightDark"
        const val speedUnit = "speedUnit"
        const val gpsUpdateInterval = "gpsUpdateInterval"
        const val hudMirror = "hudMirror"

        private val stringDefaults = mapOf(
            Pair(lightDark, "dark"),
            Pair(speedUnit, "kmh"),
            Pair(gpsUpdateInterval, "300ms")
        )

        fun get(sharedPreferences: android.content.SharedPreferences?, key: String): String {
            val default = stringDefaults[key] ?: ""
            return sharedPreferences?.getString(key, default) ?: default
        }

        const val speedEntryStartFadingAfter = 500L
        const val speedEntryTotalTimeout = 2500L
    }
}
