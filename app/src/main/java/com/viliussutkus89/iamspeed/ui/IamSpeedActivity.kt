package com.viliussutkus89.iamspeed.ui

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
import com.viliussutkus89.iamspeed.R


class IamSpeedActivity: AppCompatActivity(R.layout.activity_iamspeed) {

    private val viewModel: IamSpeedViewModel by viewModels {
        IamSpeedViewModel.Factory(application)
    }

    private val navController: NavController get() =
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController

    private val locationManagerBroadcastReceiver = object: BroadcastReceiver() {
        val intentFilter = IntentFilter().also {
            it.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                it.addAction(LocationManager.MODE_CHANGED_ACTION)
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                LocationManager.PROVIDERS_CHANGED_ACTION,
                LocationManager.MODE_CHANGED_ACTION -> {
                    viewModel.checkLocationEnabled()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBarWithNavController(navController)
        registerReceiver(locationManagerBroadcastReceiver, locationManagerBroadcastReceiver.intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationManagerBroadcastReceiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
