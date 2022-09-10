package com.viliussutkus89.iamspeed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController


class MainActivity: AppCompatActivity(R.layout.activity_main) {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    private val navController: NavController get() =
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController

    private val locationManagerBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                LocationManager.PROVIDERS_CHANGED_ACTION,
                LocationManager.MODE_CHANGED_ACTION -> {
                    viewModel.checkLocationEnabled()
                }
            }
        }

        fun register() {
            val intentFilter = IntentFilter().also {
                it.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    it.addAction(LocationManager.MODE_CHANGED_ACTION)
                }
            }
            registerReceiver(this, intentFilter)
        }

        fun unregister() {
            unregisterReceiver(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBarWithNavController(navController)
        locationManagerBroadcastReceiver.register()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManagerBroadcastReceiver.unregister()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
