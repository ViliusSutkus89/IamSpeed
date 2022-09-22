/*
 * LocationControl.kt
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

package com.viliussutkus89.iamspeed.utils

import android.app.Instrumentation
import android.os.Build
import android.provider.Settings


class LocationControl(private val instrumentation: Instrumentation) {
    private fun executeSecureSetting(setting: String) {
        if (Build.VERSION.SDK_INT >= 18) {
            instrumentation.uiAutomation.executeShellCommand(
                "settings put secure $setting"
            ).close()
        }
    }

    fun enableNetworkLocation() {
        if (Build.VERSION.SDK_INT >= 29) {
            @Suppress("DEPRECATION")
            executeSecureSetting("location_mode ${Settings.Secure.LOCATION_MODE_BATTERY_SAVING}")
        } else {
            executeSecureSetting("location_providers_allowed +network")
        }
    }

    fun enableGps() {
        if (Build.VERSION.SDK_INT >= 29) {
            @Suppress("DEPRECATION")
            executeSecureSetting("location_mode ${Settings.Secure.LOCATION_MODE_SENSORS_ONLY}")
        } else {
            executeSecureSetting("location_providers_allowed +gps")
        }
    }

    fun disable() {
        if (Build.VERSION.SDK_INT >= 29) {
            executeSecureSetting("location_mode ${Settings.Secure.LOCATION_MODE_OFF}")
        } else {
            executeSecureSetting("location_providers_allowed -gps")
        }
    }
}
