package com.viliussutkus89.speedster

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.viliussutkus89.speedster.ui.SettingsFragment


class SpeedsterApplication: MultiDexApplication() {
    private val sharedPreferences: SharedPreferences? get() = PreferenceManager.getDefaultSharedPreferences(this)

    override fun onCreate() {
        super.onCreate()
        sharedPreferences?.let {
            it.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
            updateDayNightMode(it)
        }
    }

    private val preferencesChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            if (key == SettingsFragment.dayNight) {
                (sharedPreferences ?: this.sharedPreferences)?.let {
                    updateDayNightMode(it)
                }
            }
        }

    private fun updateDayNightMode(sharedPreferences: SharedPreferences) {
        val dayNightMode = when (sharedPreferences.getString(SettingsFragment.dayNight, "auto")) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(dayNightMode)
    }
}
