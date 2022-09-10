package com.viliussutkus89.iamspeed

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.*


class MainViewModel(app: Application): AndroidViewModel(app) {
    companion object {
        private const val TAG = "MainViewModel"
    }

    val started: LiveData<Boolean> get() = SpeedListenerService.started
    val speed: LiveData<SpeedListenerService.SpeedEntry?> get() = SpeedListenerService.speed
    val satelliteCount: LiveData<Int> get() = SpeedListenerService.satelliteCount

    private val app: Application get() = getApplication<Application>()

    class Factory(private val app: Application): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct MainViewModel")
        }
    }

    private val _showLocationPermissionRequest = MutableLiveData(true)
    val showLocationPermissionRequest: LiveData<Boolean> get() = _showLocationPermissionRequest

    private val _showFineLocationPermissionRequest = MutableLiveData(false)
    val showFineLocationPermissionRequest: LiveData<Boolean> get() = _showFineLocationPermissionRequest

    fun checkPermissions() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                _showLocationPermissionRequest.value = false
                _showFineLocationPermissionRequest.value = false
            }
            ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                _showLocationPermissionRequest.value = false
                _showFineLocationPermissionRequest.value = true
            }
            else -> {
                _showLocationPermissionRequest.value = true
                _showFineLocationPermissionRequest.value = false
                stop()
            }
        }
    }

    private val _showEnableLocationRequest = MutableLiveData(true)
    val showEnableLocationRequest: LiveData<Boolean> get() = _showEnableLocationRequest

    private val _showEnableGpsLocationRequest = MutableLiveData(true)
    val showEnableGpsLocationRequest: LiveData<Boolean> get() = _showEnableGpsLocationRequest

    fun checkLocationEnabled() {
        getSystemService(app, LocationManager::class.java)?.let { lm ->
            val isLocationAvailable = LocationManagerCompat.isLocationEnabled(lm)
            val isGpsProviderEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            _showEnableLocationRequest.value = !isLocationAvailable
            _showEnableGpsLocationRequest.value = isLocationAvailable && !isGpsProviderEnabled
        } ?: run {
            Log.e(TAG, "Failed to obtain LocationManager")
        }
    }

    val serviceCanBeStartedOnStartup = MergerLiveData.Three(
        _showLocationPermissionRequest,
        _showEnableLocationRequest,
        _showEnableGpsLocationRequest,
    ) {
        noLocationPermission,
        locationDisabled,
        noGps ->

        !(noLocationPermission || locationDisabled || noGps)
    }

    val serviceCanBeStarted = MergerLiveData.Four(
        _showLocationPermissionRequest,
        _showEnableLocationRequest,
        _showEnableGpsLocationRequest,
        started
    ) {
        noLocationPermission,
        locationDisabled,
        noGps,
        alreadyStarted ->

        !(noLocationPermission || locationDisabled || noGps || alreadyStarted)
    }

    fun start() {
        SpeedListenerService.startSpeedListener(app)
    }

    fun stop() {
        SpeedListenerService.stopSpeedListener(app)
    }
}
