package com.viliussutkus89.iamspeed.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.viliussutkus89.iamspeed.BuildConfig
import com.viliussutkus89.iamspeed.R
import com.viliussutkus89.iamspeed.databinding.FragmentAboutBinding


class AboutFragment: Fragment(R.layout.fragment_about) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAboutBinding.inflate(inflater, container, false).apply {
            version = BuildConfig.VERSION_NAME
        }.root
    }
}
