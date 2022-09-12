package com.viliussutkus89.iamspeed

import android.content.SharedPreferences

class Settings {
    companion object {
        const val lightDark = "lightDark"
        const val speedUnit = "speedUnit"
        const val gpsUpdateInterval = "gpsUpdateInterval"

        private val defaults = mapOf(
            Pair(lightDark, "dark"),
            Pair(speedUnit, "kmh"),
            Pair(gpsUpdateInterval, "300ms")
        )

        fun get(sharedPreferences: SharedPreferences?, key: String): String {
            val default = defaults[key] ?: ""
            return sharedPreferences?.getString(key, default) ?: default
        }
    }
}