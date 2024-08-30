/*
 * Pip.kt
 *
 * Copyright (C) 2022, 2024 https://www.ViliusSutkus89.com/i-am-speed/
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import com.viliussutkus89.iamspeed.databinding.PipBinding
import com.viliussutkus89.iamspeed.service.SpeedEntry


class Pip(
    parentLifecycleOwner: LifecycleOwner,
    private val serviceContext: Context,
    private val speed: LiveData<SpeedEntry?>
): LifecycleOwner {
    private val windowManager: WindowManager =
        serviceContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var _binding: PipBinding? = null
    private val binding: PipBinding get() = _binding!!

    private val lifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    init {
        parentLifecycleOwner.lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                start()
            }
            override fun onStop(owner: LifecycleOwner) {
                stop()
            }
        })
    }

    private fun getLayoutParams(): WindowManager.LayoutParams {
        val layoutType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_TOAST
        }

        val flag: Int = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        val format = PixelFormat.RGB_888

        return WindowManager.LayoutParams(
            300, 300,
            layoutType,
            flag,
            format
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    fun start() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        _binding = PipBinding.inflate(LayoutInflater.from(serviceContext))
        windowManager.addView(binding.root, getLayoutParams().also {
            it.gravity = Gravity.CENTER
        })

        speed.observe(this) { speedEntry: SpeedEntry? ->
            binding.speed.text = speedEntry?.speedInt?.toString() ?: "???"
        }

        binding.speed.setOnClickListener {
            serviceContext.startActivity(
                Intent(serviceContext, IamSpeedActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }

        binding.root.setOnLongClickListener {
            binding.root.setOnTouchListener(onDragListener)
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onDragListener: View.OnTouchListener = View.OnTouchListener { v, event ->
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                windowManager.updateViewLayout(
                    binding.root,
                    getLayoutParams().apply {
                        gravity = Gravity.TOP or Gravity.START
                        x = event.rawX.toInt() - (binding.root.measuredWidth / 2)
                        y = event.rawY.toInt() - (binding.root.measuredHeight)
                    }
                )
            }
            MotionEvent.ACTION_UP -> {
                binding.root.setOnTouchListener(null)
            }
        }
        true
    }

    fun stop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        _binding?.let {
            windowManager.removeView(it.root)
            _binding = null
        }
    }
}
