package com.viliussutkus89.iamspeed.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.viliussutkus89.iamspeed.R
import com.viliussutkus89.iamspeed.AppSettings


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
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
        arrayOf(AppSettings.lightDark, AppSettings.speedUnit, AppSettings.gpsUpdateInterval).forEach {
            findPreference<ListPreference>(it)?.let { pref ->
                pref.summary = pref.entry ?: ""
            }
        }
    }
}
