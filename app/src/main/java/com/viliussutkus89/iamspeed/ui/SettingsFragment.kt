package com.viliussutkus89.iamspeed.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.viliussutkus89.iamspeed.R


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val dayNight = "dayNight"
        const val speedUnit = "speedUnit"
        const val gpsUpdateInterval = "gpsUpdateInterval"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        updateSummaries()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        updateSummaries()
    }

    private fun updateSummaries() {
        arrayOf(dayNight, speedUnit, gpsUpdateInterval).forEach {
            findPreference<ListPreference>(it)?.let { pref ->
                pref.summary = pref.entry ?: ""
            }
        }
    }
}
