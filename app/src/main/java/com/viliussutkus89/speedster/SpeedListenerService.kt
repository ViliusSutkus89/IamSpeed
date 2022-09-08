package com.viliussutkus89.speedster

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.location.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.viliussutkus89.speedster.ui.SettingsFragment
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class SpeedListenerService: Service() {
    data class SpeedEntry(
        val speedInt: Int,
        val speedStr: String,
        val accuracy: String?
    )

    private fun createSpeedEntry(speed: Float, accuracyMetersPerSecond: Float?): SpeedEntry {
        val speedInt: Int
        val speedStr: String
        val accuracy: String?

        when (speedUnit) {
            SpeedUnitType.MS -> {
                speedInt = speed.toInt()
                speedStr = "$speedInt m/s"
                accuracy = accuracyMetersPerSecond?.toString()
            }
            SpeedUnitType.KMH -> {
                speedInt = (speed * 3.6f).toInt()
                speedStr = "$speedInt km/h"
                accuracy = accuracyMetersPerSecond?.let { (it * 3.6f).toString() }
            }
            SpeedUnitType.MPH -> {
                speedInt = (speed * 2.2369f).toInt()
                speedStr = "$speedInt mph"
                accuracy = accuracyMetersPerSecond?.let { (it * 2.2369f).toString() }
            }
        }

        return SpeedEntry(
            speedInt = speedInt,
            speedStr = speedStr,
            accuracy = accuracy
        )
    }

    companion object {
        private const val TAG = "SpeedListenerService"

        private const val notificationId = 1

        private val started_ = MutableLiveData(false)
        val started: LiveData<Boolean> get() = started_

        private val speed_ = MutableLiveData<SpeedEntry?>(null)
        val speed: LiveData<SpeedEntry?> get() = speed_

        private val satelliteCount_ = MutableLiveData(0)
        val satelliteCount: LiveData<Int> = satelliteCount_

        private const val START_INTENT_ACTION = "START"
        fun startSpeedListener(context: Context) {
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = START_INTENT_ACTION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        private const val STOP_INTENT_ACTION = "STOP"
        fun stopSpeedListener(context: Context) {
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = STOP_INTENT_ACTION
            }
            context.startService(intent)
        }

        const val STOP_BROADCAST_ACTION = "com.viliussutkus89.speedster.STOP_BROADCAST"
    }

    private val stopBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == STOP_BROADCAST_ACTION) {
                stop()
            }
        }

        fun register() {
            this@SpeedListenerService.registerReceiver(this, IntentFilter().also {
                it.addAction(STOP_BROADCAST_ACTION)
            })
        }

        fun unregister() {
            this@SpeedListenerService.unregisterReceiver(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String) {
        val name: CharSequence = getString(R.string.speedster_notification_channel_name)
        val description = getString(R.string.speedster_notification_channel_description)
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
        channel.description = description
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        val channelId = getString(R.string.speedster_notification_channel_id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId)
        }

        val mutabilityFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val tapActionIntent = Intent(this, MainActivity::class.java)
        val tapActionPendingIntent = PendingIntent.getActivity(this, 0, tapActionIntent, mutabilityFlag)

        val stopIntent = Intent().apply {
            action = STOP_BROADCAST_ACTION
        }
        val stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, mutabilityFlag)
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon_speedster)
            .setContentIntent(tapActionPendingIntent)
            .addAction(R.drawable.notification_icon_off, getString(R.string.stop), stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    private fun getNotification(speedEntry: SpeedEntry?): Notification {
        val notificationTitle = speedEntry?.speedStr ?: getString(R.string.waiting_for_signal)
        return notificationBuilder
            .setTicker(notificationTitle)
            .setContentTitle(notificationTitle)
            .build()
    }

    private enum class SpeedUnitType {
        KMH, MS, MPH
    }
    private lateinit var speedUnit: SpeedUnitType
    private fun getSpeedUnit(sharedPreferences: SharedPreferences) {
        speedUnit = when (sharedPreferences.getString(SettingsFragment.speedUnit, "kmh")) {
            "kmh" -> SpeedUnitType.KMH
            "mph" -> SpeedUnitType.MPH
            else -> SpeedUnitType.MS
        }
    }

    private val sharedPreferences: SharedPreferences? get() = PreferenceManager.getDefaultSharedPreferences(this)

    private val executor: Executor = Executors.newSingleThreadExecutor()

    private fun requestLocationUpdates(locationManager: LocationManager) {
        val intervalStr = sharedPreferences?.getString(SettingsFragment.gpsUpdateInterval, null)
        val interval = (intervalStr?.removeSuffix("ms") ?: "300").toLong()

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                START_INTENT_ACTION -> start()
                STOP_INTENT_ACTION -> stop()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun start() {
        if (started_.value == true) {
            Log.e(TAG, "Double start event detected!")
            return
        }

        sharedPreferences?.let { sp ->
            sp.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
            getSpeedUnit(sp)
        }

        val initialNotification = notificationBuilder
            .setTicker(getString(R.string.waiting_for_signal))
            .setContentTitle(getString(R.string.waiting_for_signal))
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, initialNotification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(notificationId, initialNotification)
        }

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        requestLocationUpdates(locationManager)
        stopBroadcastReceiver.register()
        try {
            LocationManagerCompat.registerGnssStatusCallback(locationManager, executor, gnssStatusCallback)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        started_.postValue(true)
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

    private fun stop() {
        if (started_.value != true) {
            Log.e(TAG, "Double stop event detected!")
            return
        }

        stopBroadcastReceiver.unregister()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            LocationManagerCompat.removeUpdates(locationManager, locationChangeListener)
            LocationManagerCompat.unregisterGnssStatusCallback(locationManager, gnssStatusCallback)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        started_.postValue(false)
        satelliteCount_.postValue(0)
        speed_.postValue(null)
        NotificationManagerCompat.from(this).cancel(notificationId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private val preferencesChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            (sharedPreferences ?: this.sharedPreferences)?.let { sp ->
                when (key) {
                    SettingsFragment.speedUnit -> getSpeedUnit(sp)
                    SettingsFragment.gpsUpdateInterval -> {
                        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                        try {
                            LocationManagerCompat.removeUpdates(locationManager, locationChangeListener)
                            requestLocationUpdates(locationManager)
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

    private val locationChangeListener = LocationListenerCompat { location ->
        if (location.hasSpeed() && started_.value == true) {
            val accuracy = if (LocationCompat.hasSpeedAccuracy(location)) {
                LocationCompat.getSpeedAccuracyMetersPerSecond(location)
            } else {
                null
            }

            createSpeedEntry(location.speed, accuracy).let { speedEntry ->
                speed_.postValue(speedEntry)
                NotificationManagerCompat.from(this)
                    .notify(notificationId, getNotification(speedEntry))
            }
        }
    }
}
