/*
 * SatelliteCountListener.kt
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

package com.viliussutkus89.iamspeed.service

import android.location.LocationManager
import androidx.annotation.MainThread
import androidx.core.location.GnssStatusCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executor


internal class SatelliteCountListener(
    private val locationManager: LocationManager,
    private val executor: Executor
) {

    companion object {
        private val satelliteCount_ = MutableLiveData(0)
        val satelliteCount: LiveData<Int> = satelliteCount_
    }

    private val gnssStatusCallback = object: GnssStatusCompat.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatusCompat) {
            /*
            https://developer.android.com/reference/androidx/core/location/GnssStatusCompat
            Note: When used to wrap GpsStatus, the best performance can be obtained by using a monotonically increasing satelliteIndex parameter (for instance, by using a loop from 0 to getSatelliteCount). Random access is supported but performance may suffer.
             */
            var active = 0
            for (i in 0 until status.satelliteCount) {
                if (status.usedInFix(i)) {
                    active++
                }
            }
            satelliteCount_.postValue(active)
        }
    }

    @MainThread
    fun start() {
        try {
            LocationManagerCompat.registerGnssStatusCallback(locationManager, executor, gnssStatusCallback)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @MainThread
    fun stop() {
        try {
            LocationManagerCompat.unregisterGnssStatusCallback(locationManager, gnssStatusCallback)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        satelliteCount_.postValue(0)
    }
}
