package com.viliussutkus89.iamspeed.service

import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viliussutkus89.iamspeed.Settings
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


internal class SpeedListener(
    private val locationManager: LocationManager,
    private val executor: ScheduledExecutorService,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private val speed_ = MutableLiveData<SpeedEntry?>(null)
        val speed: LiveData<SpeedEntry?> get() = speed_
    }

    private val speedUnit = SpeedUnit(sharedPreferences)

    private var speedEntryClearerFuture: ScheduledFuture<*> ? = null
    private val speedEntryClearerRunnable = {
        speed_.postValue(null)
    }

    private val gpsUpdateIntervalChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? ->
            if (key == Settings.gpsUpdateInterval) {
                stopLocationUpdates()
                requestLocationUpdates()
            }
        }

    fun start() {
        speedUnit.start()
        requestLocationUpdates()
        sharedPreferences.registerOnSharedPreferenceChangeListener(gpsUpdateIntervalChangeListener)
    }

    fun stop() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(gpsUpdateIntervalChangeListener)
        stopLocationUpdates()
        speedUnit.stop()
        speed_.value = null
    }

    private val locationChangeListener = LocationListenerCompat { location ->
        if (location.hasSpeed() && SpeedListenerService.started.value == true) {
            val accuracy = if (LocationCompat.hasSpeedAccuracy(location)) {
                LocationCompat.getSpeedAccuracyMetersPerSecond(location)
            } else {
                null
            }
            onSpeed(location.speed, accuracy)
        }
    }

    private fun onSpeed(speed: Float, accuracy: Float?) {
        speedUnit.translate(speed, accuracy).let { speedEntry ->
            speedEntryClearerFuture?.cancel(false)
            speed_.postValue(speedEntry)
            speedEntryClearerFuture = executor.schedule(speedEntryClearerRunnable, Settings.speedEntryTotalTimeout, TimeUnit.MILLISECONDS)
        }
    }

    private fun requestLocationUpdates() {
        val interval = Settings.get(sharedPreferences, Settings.gpsUpdateInterval)
            .removeSuffix("ms").toLong()

        val locationRequest = LocationRequestCompat
            .Builder(interval)
            .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY).build()

        try {
            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                LocationManager.GPS_PROVIDER,
                locationRequest,
                executor,
                locationChangeListener,
            )

            LocationManagerCompat.getCurrentLocation(
                locationManager,
                LocationManager.GPS_PROVIDER,
                null,
                executor
            ) { it: Location? ->
                it?.let {
                    locationChangeListener.onLocationChanged(it)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        try {
            LocationManagerCompat.removeUpdates(locationManager, locationChangeListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
