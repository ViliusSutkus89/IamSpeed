package com.viliussutkus89.iamspeed.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viliussutkus89.iamspeed.MergerLiveData
import com.viliussutkus89.iamspeed.service.SpeedEntry
import com.viliussutkus89.iamspeed.service.SpeedListenerService


class IamSpeedViewModel: ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    val started: LiveData<Boolean> get() = SpeedListenerService.started
    val speed: LiveData<SpeedEntry?> get() = SpeedListenerService.speed
    val satelliteCount: LiveData<Int> get() = SpeedListenerService.satelliteCount

    private val _showLocationPermissionRequest = MutableLiveData(true)
    val showLocationPermissionRequest: LiveData<Boolean> get() = _showLocationPermissionRequest

    private val _showFineLocationPermissionRequest = MutableLiveData(false)
    val showFineLocationPermissionRequest: LiveData<Boolean> get() = _showFineLocationPermissionRequest

    fun checkPermissions(context: Context) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                _showLocationPermissionRequest.value = false
                _showFineLocationPermissionRequest.value = false
            }
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                _showLocationPermissionRequest.value = false
                _showFineLocationPermissionRequest.value = true
            }
            else -> {
                _showLocationPermissionRequest.value = true
                _showFineLocationPermissionRequest.value = false
                stop(context)
            }
        }
    }

    private val _showEnableLocationRequest = MutableLiveData(true)
    val showEnableLocationRequest: LiveData<Boolean> get() = _showEnableLocationRequest

    private val _showEnableGpsLocationRequest = MutableLiveData(true)
    val showEnableGpsLocationRequest: LiveData<Boolean> get() = _showEnableGpsLocationRequest

    fun checkLocationEnabled(context: Context) {
        getSystemService(context, LocationManager::class.java)?.let { lm ->
            val isLocationAvailable = LocationManagerCompat.isLocationEnabled(lm)
            val isGpsProviderEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            _showEnableLocationRequest.value = !isLocationAvailable
            _showEnableGpsLocationRequest.value = isLocationAvailable && !isGpsProviderEnabled
        } ?: run {
            Log.e(TAG, "Failed to obtain LocationManager")
        }
    }

    val serviceCanBeStartedOnStartup = MergerLiveData.Three(
        showLocationPermissionRequest,
        showEnableLocationRequest,
        showEnableGpsLocationRequest,
    ) { noLocationPermission,
        locationDisabled,
        noGps ->

        !(noLocationPermission || locationDisabled || noGps)
    }

    val serviceCanBeStarted = MergerLiveData.Four(
        showLocationPermissionRequest,
        showEnableLocationRequest,
        showEnableGpsLocationRequest,
        started
    ) { noLocationPermission,
        locationDisabled,
        noGps,
        alreadyStarted ->

        !(noLocationPermission || locationDisabled || noGps || alreadyStarted)
    }

    fun start(context: Context) {
        SpeedListenerService.startSpeedListener(context)
    }

    fun stop(context: Context) {
        SpeedListenerService.stopSpeedListener(context)
    }
}
