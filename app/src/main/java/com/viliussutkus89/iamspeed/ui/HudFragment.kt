/*
 * HudFragment.kt
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

import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.viliussutkus89.iamspeed.AppSettings
import com.viliussutkus89.iamspeed.AppSettings.Companion.speedEntryStartFadingAfter
import com.viliussutkus89.iamspeed.AppSettings.Companion.speedEntryTotalTimeout
import com.viliussutkus89.iamspeed.R


class HudFragment : Fragment(R.layout.fragment_hud) {
    private val isRunningTest: Boolean = try {
        Class.forName("androidx.test.espresso.Espresso")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    private val viewModel by activityViewModels<IamSpeedViewModel>()

    private var systemUiVisibilityBeforeTouching: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.stopCountingSatellites(requireContext())

        (requireActivity() as AppCompatActivity).let { activity ->
            activity.supportActionBar?.hide()
            if (Build.VERSION.SDK_INT >= 30) {
                activity.window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                systemUiVisibilityBeforeTouching = activity.window.decorView.systemUiVisibility

                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

                if (Build.VERSION.SDK_INT >= 16) {
                    @Suppress("DEPRECATION")
                    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                } else {
                    @Suppress("DEPRECATION")
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.restartCountingSatellites(requireContext())

        (requireActivity() as AppCompatActivity).let { activity ->
            activity.supportActionBar?.show()
            activity.window.let { window ->
                if (Build.VERSION.SDK_INT >= 30) {
                    // empty run at the end because IDE won't shut up about "SDK_INT < 16" check below
                    window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()) ?: run {}
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = systemUiVisibilityBeforeTouching
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = systemUiVisibilityBeforeTouching

                    if (Build.VERSION.SDK_INT < 16) {
                        @Suppress("DEPRECATION")
                        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val speedView = view.findViewById<TextView>(R.id.speed)

        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(AppSettings.hudMirror, true)) {
            speedView.scaleY = -1f
        }

        viewModel.speed.observe(viewLifecycleOwner) { speed ->
            speedView.clearAnimation()
            speedView.text = speed?.let {
                if (!isRunningTest) {
                    speedView.startAnimation(fadeoutAnimation)
                }
                it.speedInt.toString()
            } ?: "???"
        }
    }

    private val fadeoutAnimation = AlphaAnimation(1.0f, 0.1f).also {
        it.duration = speedEntryTotalTimeout - speedEntryStartFadingAfter
        it.startOffset = speedEntryStartFadingAfter
    }
}
