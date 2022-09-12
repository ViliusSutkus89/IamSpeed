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
            if (key == Settings.dayNight) {
                updateDayNightMode(sharedPreferences)
            }
        }

    private fun updateDayNightMode(sharedPreferences: SharedPreferences?) {
        val dayNightMode = when (Settings.get(sharedPreferences, Settings.dayNight)) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(dayNightMode)
    }
}
