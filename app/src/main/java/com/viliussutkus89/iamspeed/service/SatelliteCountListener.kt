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
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
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
        private const val TAG = "SatelliteCountListener"
        private val satelliteCount_ = MutableLiveData<SatelliteCount?>(null)
        val satelliteCount: LiveData<SatelliteCount?> = satelliteCount_
    }

    private val gnssStatusCallback = object: GnssStatusCompat.Callback() {
        @WorkerThread
        override fun onSatelliteStatusChanged(status: GnssStatusCompat) {
            /*
            https://developer.android.com/reference/androidx/core/location/GnssStatusCompat
            Note: When used to wrap GpsStatus, the best performance can be obtained by using a monotonically increasing satelliteIndex parameter (for instance, by using a loop from 0 to getSatelliteCount). Random access is supported but performance may suffer.
             */
            val total = status.satelliteCount
            var active = 0
            for (i in 0 until total) {
                if (status.usedInFix(i)) {
                    active++
                }
            }
            satelliteCount_.postValue(SatelliteCount(active, total))
        }
    }

    private var started = false

    @MainThread
    fun start() {
        if (!started) {
            Log.w(TAG, "Double start event detected!")
            return
        }
        try {
            LocationManagerCompat.registerGnssStatusCallback(locationManager, executor, gnssStatusCallback)
            started = true
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
        started = false
        satelliteCount_.value = null
    }
}
