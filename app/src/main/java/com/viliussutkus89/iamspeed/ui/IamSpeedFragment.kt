package com.viliussutkus89.iamspeed.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.viliussutkus89.iamspeed.R
import com.viliussutkus89.iamspeed.databinding.FragmentIamSpeedBinding


class IamSpeedFragment: Fragment() {
    private var _binding: FragmentIamSpeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IamSpeedViewModel by activityViewModels {
        IamSpeedViewModel.Factory(requireActivity().application)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.checkPermissions()
        viewModel.checkLocationEnabled()
        _binding = FragmentIamSpeedBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
        return binding.root
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkPermissions()
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
            viewModel.start()
        }

        viewModel.serviceCanBeStartedOnStartup.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.start()
            }
        }

        viewModel.speed.observe(viewLifecycleOwner) { speed ->
            binding.speed.let { speedBinding ->
                speedBinding.clearAnimation()
                speedBinding.text = speed?.let {
                    speedBinding.startAnimation(getNewAnimationObj())
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

    private var speedFadeoutAnimation: Animation? = null
    private fun getNewAnimationObj(): Animation {
        speedFadeoutAnimation?.let {
            it.setAnimationListener(null)
            it.cancel()
        }
        val animation = AlphaAnimation(1.0f, 0.1f).also {
            it.duration = 2000
            it.startOffset = 500
            it.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) = Unit
                override fun onAnimationRepeat(animation: Animation?) = Unit
                override fun onAnimationEnd(animation: Animation?) {
                    binding.speed.text = "???"
                }
            })
        }
        speedFadeoutAnimation = animation
        return animation
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
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.stop -> {
                    viewModel.stop()
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
