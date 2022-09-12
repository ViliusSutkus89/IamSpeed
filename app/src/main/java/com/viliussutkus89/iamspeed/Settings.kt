package com.viliussutkus89.iamspeed

import android.content.SharedPreferences

class Settings {
    companion object {
        const val dayNight = "dayNight"
        const val speedUnit = "speedUnit"
        const val gpsUpdateInterval = "gpsUpdateInterval"

        private val defaults = mapOf(
            Pair(dayNight, "dark"),
            Pair(speedUnit, "kmh"),
            Pair(gpsUpdateInterval, "300ms")
        )

        fun get(sharedPreferences: SharedPreferences?, key: String): String {
            val default = defaults[key] ?: ""
            return sharedPreferences?.getString(key, default) ?: default
        }
    }
}