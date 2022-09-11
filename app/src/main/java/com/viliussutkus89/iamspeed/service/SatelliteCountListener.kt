package com.viliussutkus89.iamspeed.service

import android.location.LocationManager
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

    fun start() {
        try {
            LocationManagerCompat.registerGnssStatusCallback(locationManager, executor, gnssStatusCallback)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            LocationManagerCompat.unregisterGnssStatusCallback(locationManager, gnssStatusCallback)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        satelliteCount_.postValue(0)
    }
}
