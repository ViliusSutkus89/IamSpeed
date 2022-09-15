package com.viliussutkus89.iamspeed

import android.content.SharedPreferences

class AppSettings {
    companion object {
        const val lightDark = "lightDark"
        const val speedUnit = "speedUnit"
        const val gpsUpdateInterval = "gpsUpdateInterval"

        private val stringDefaults = mapOf(
            Pair(lightDark, "dark"),
            Pair(speedUnit, "kmh"),
            Pair(gpsUpdateInterval, "300ms")
        )

        fun get(sharedPreferences: SharedPreferences?, key: String): String {
            val default = stringDefaults[key] ?: ""
            return sharedPreferences?.getString(key, default) ?: default
        }

        const val speedEntryStartFadingAfter = 500L
        const val speedEntryTotalTimeout = 2500L
    }
}