/*
 * SpeedListenerService.kt
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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import androidx.test.espresso.IdlingResource
import com.viliussutkus89.iamspeed.R
import com.viliussutkus89.iamspeed.ui.IamSpeedActivity
import com.viliussutkus89.iamspeed.ui.Pip
import com.viliussutkus89.iamspeed.util.CountingIdlingResourceFactory
import java.util.concurrent.Executors


class SpeedListenerService: LifecycleService() {

    companion object {
        private const val TAG = "SpeedListenerService"

        private const val notificationId = 1

        private val started_ = MutableLiveData(false)
        val started: LiveData<Boolean> get() = started_

        val speed: LiveData<SpeedEntry?> get() = SpeedListener.speed
        val satelliteCount: LiveData<SatelliteCount?> get() = SatelliteCountListener.satelliteCount

        private const val START_INTENT_ACTION = "START"
        @MainThread
        fun startSpeedListener(context: Context) {
            startingIdlingResource?.increment()
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = START_INTENT_ACTION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        @VisibleForTesting
        internal const val STOP_BROADCAST_ACTION = "com.viliussutkus89.iamspeed.STOP_BROADCAST"
        private const val STOP_INTENT_ACTION = "STOP"
        @MainThread
        fun stopSpeedListener(context: Context) {
            stoppingIdlingResource?.increment()
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = STOP_INTENT_ACTION
            }
            context.startService(intent)
        }

        private const val MOCK_INTENT_ACTION = "MOCK"
        @MainThread
        fun mockSpeed(context: Context) {
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = MOCK_INTENT_ACTION
            }
            context.startService(intent)
        }

        private const val STOP_COUNTING_SATELLITES = "STOP_COUNTING_SATELLITES"
        @MainThread
        fun stopCountingSatellites(context: Context) {
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = STOP_COUNTING_SATELLITES
            }
            context.startService(intent)
        }

        private const val RESTART_COUNTING_SATELLITES = "RESTART_COUNTING_SATELLITES"
        @MainThread
        fun restartCountingSatellites(context: Context) {
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = RESTART_COUNTING_SATELLITES
            }
            context.startService(intent)
        }

        private const val PIP_INTENT_ACTION = "PIP"
        @MainThread
        fun enterPipMode(context: Context) {
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = PIP_INTENT_ACTION
            }
            context.startService(intent)
        }

        private const val PIP_CLOSE_INTENT_ACTION = "PIP_CLOSE"
        @MainThread
        fun closePipMode(context: Context) {
            val intent = Intent(context, SpeedListenerService::class.java).also {
                it.action = PIP_CLOSE_INTENT_ACTION
            }
            context.startService(intent)
        }

        private val startingIdlingResource = CountingIdlingResourceFactory.create("${this::class.java.declaringClass}.starting")
        private val stoppingIdlingResource = CountingIdlingResourceFactory.create("${this::class.java.declaringClass}.stopping")

        @VisibleForTesting
        internal val locationSettingsChangedIdlingResource = CountingIdlingResourceFactory.create("${this::class.java.declaringClass}.locationSettingsChanged")

        @VisibleForTesting
        internal val idlingResources: List<IdlingResource> = listOfNotNull(
            startingIdlingResource,
            stoppingIdlingResource,
            locationSettingsChangedIdlingResource
        )
    }

    private val stopBroadcastReceiver = object: BroadcastReceiver() {
        val intentFilter = IntentFilter().also {
            it.addAction(STOP_BROADCAST_ACTION)
            it.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }

        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                STOP_BROADCAST_ACTION -> {
                    stoppingIdlingResource?.increment()
                    stop()
                }

                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        stoppingIdlingResource?.increment()
                        stop()
                    }
                    locationSettingsChangedIdlingResource?.decrement()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String) {
        val name: CharSequence = getString(R.string.notification_channel_name)
        val description = getString(R.string.notification_channel_description)
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
        channel.description = description
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        val channelId = getString(R.string.notification_channel_id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId)
        }

        val mutabilityFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val tapActionIntent = Intent(this, IamSpeedActivity::class.java)
        val tapActionPendingIntent = PendingIntent.getActivity(this, 0, tapActionIntent, mutabilityFlag)

        val stopIntent = Intent().apply {
            action = STOP_BROADCAST_ACTION
        }
        val stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, mutabilityFlag)
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon_iamspeed)
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

    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(this) }

    // These are lazy loaded, because Context isn't ready yet
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private val executor by lazy { Executors.newSingleThreadScheduledExecutor() }
    private val sharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(this)
    private val speedListener by lazy { SpeedListener(locationManager, executor, sharedPreferences) }
    private val satelliteCountListener by lazy { SatelliteCountListener(locationManager, executor) }

    private var pip: Pip? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                START_INTENT_ACTION -> start()
                STOP_INTENT_ACTION -> stop()
                STOP_COUNTING_SATELLITES -> satelliteCountListener.stop()
                RESTART_COUNTING_SATELLITES -> satelliteCountListener.start()
                PIP_INTENT_ACTION -> {
                    pip = Pip(this, this, speed)
                }

                PIP_CLOSE_INTENT_ACTION -> {
                    pip?.stop()
                    pip = null
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        if (started.value == true) {
            Log.e(TAG, "Double start event detected!")
            return
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

        registerReceiver(stopBroadcastReceiver, stopBroadcastReceiver.intentFilter)
        speedListener.start()
        satelliteCountListener.start()

        speed.observe(this) { speedEntry ->
            notificationManagerCompat.notify(notificationId, getNotification(speedEntry))
        }

        started_.value = true
        startingIdlingResource?.decrement()
    }

    private fun stop() {
        if (started.value != true) {
            return
        }

        unregisterReceiver(stopBroadcastReceiver)
        speedListener.stop()
        satelliteCountListener.stop()
        executor.shutdownNow()
        notificationManagerCompat.cancel(notificationId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stoppingIdlingResource?.decrement()
        started_.value = false
        stopSelf()
    }
}
