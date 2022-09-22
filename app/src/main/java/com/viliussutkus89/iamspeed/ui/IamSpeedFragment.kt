/*
 * IamSpeedFragment.kt
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

package com.viliussutkus89.iamspeed.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.animation.AlphaAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.viliussutkus89.iamspeed.AppSettings.Companion.speedEntryStartFadingAfter
import com.viliussutkus89.iamspeed.AppSettings.Companion.speedEntryTotalTimeout
import com.viliussutkus89.iamspeed.R
import com.viliussutkus89.iamspeed.databinding.FragmentIamSpeedBinding


class IamSpeedFragment: Fragment() {
    private var _binding: FragmentIamSpeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<IamSpeedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().let { context ->
            viewModel.checkPermissions(context)
            viewModel.checkLocationEnabled(context)
            viewModel.serviceCanBeStartedOnStartup.observe(viewLifecycleOwner) { serviceCanBeStartedOnStartup ->
                if (serviceCanBeStartedOnStartup && viewModel.started.value != true) {
                    viewModel.start(context)
                }

                (activity as IamSpeedActivity?)?.serviceCanBeStartedOnStartupIdlingResource?.let {
                    if (!it.isIdleNow) {
                        it.decrement()
                    }
                }
            }
            context.addMenuProvider(menuProvider, viewLifecycleOwner)
            context.registerReceiver(locationManagerBroadcastReceiver, locationManagerBroadcastReceiver.intentFilter)
        }
        _binding = FragmentIamSpeedBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(locationManagerBroadcastReceiver)
        _binding = null
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkPermissions(requireContext())
    }

    private val isRunningTest: Boolean = try {
        Class.forName("androidx.test.espresso.Espresso")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val requestLocation = { _: View ->
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        binding.requestLocationPermission.setOnClickListener(requestLocation)
        binding.requestFineLocationPermission.setOnClickListener(requestLocation)

        val openSettings = { _: View ->
            try {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(binding.root, getString(R.string.failed_to_open_location_settings), Snackbar.LENGTH_LONG).show()
            }
        }
        binding.buttonEnableLocation.setOnClickListener(openSettings)
        binding.buttonEnableGps.setOnClickListener(openSettings)
        binding.buttonStart.setOnClickListener {
            viewModel.start(requireContext())
        }

        viewModel.speed.observe(viewLifecycleOwner) { speed ->
            binding.speed.let { speedBinding ->
                speedBinding.clearAnimation()
                speedBinding.text = speed?.let {
                    if (!isRunningTest) {
                        speedBinding.startAnimation(fadeoutAnimation)
                    }
                    it.speedInt.toString()
                } ?: "???"
            }

            binding.accuracy.let { accuracyBinding ->
                speed?.accuracy?.let { accuracy ->
                    accuracyBinding.visibility = View.VISIBLE
                    accuracyBinding.text = getString(R.string.accuracy, accuracy)
                } ?: let {
                    accuracyBinding.visibility = View.GONE
                    accuracyBinding.text = ""
                }
            }
        }
    }

    private val locationManagerBroadcastReceiver = object: BroadcastReceiver() {
        val intentFilter = IntentFilter().also {
            it.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.checkLocationEnabled(requireContext())
            (activity as IamSpeedActivity?)?.locationSettingsChangedIdlingResource?.let {
                if (!it.isIdleNow) {
                    it.decrement()
                }
            }
        }
    }

    private val fadeoutAnimation = AlphaAnimation(1.0f, 0.1f).also {
        it.duration = speedEntryTotalTimeout - speedEntryStartFadingAfter
        it.startOffset = speedEntryStartFadingAfter
    }

    private val menuProvider = object: MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main_menu, menu)
            // Workaround for unavailable Hamburger menu in API-16
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                menu.forEach {
                    it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
            }

            viewModel.started.observe(viewLifecycleOwner) {
                menu.findItem(R.id.stop)?.isVisible = it
                menu.findItem(R.id.hud)?.isVisible = it
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.hud -> {
                    findNavController().navigate(IamSpeedFragmentDirections.actionIamSpeedFragmentToHudFragment())
                    true
                }
                R.id.stop -> {
                    viewModel.stop(requireContext())
                    true
                }
                R.id.settings -> {
                    findNavController().navigate(IamSpeedFragmentDirections.actionIamSpeedFragmentToSettingsFragment())
                    true
                }
                R.id.about -> {
                    findNavController().navigate(IamSpeedFragmentDirections.actionIamSpeedFragmentToAboutFragment())
                    true
                }
                else -> false
            }
        }
    }
}
