/*
 * IamSpeedActivity.kt
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

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.viliussutkus89.iamspeed.R
import com.viliussutkus89.iamspeed.util.CountingIdlingResourceFactory


class IamSpeedActivity: AppCompatActivity(R.layout.activity_iamspeed) {
    private val navController: NavController get() =
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal val locationSettingsChangedIdlingResource = CountingIdlingResourceFactory.create("${this::class.java.declaringClass}.locationSettingsChanged")

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal val serviceCanBeStartedOnStartupIdlingResource = CountingIdlingResourceFactory.create("${this::class.java.declaringClass}.serviceCanBeStartedOnStartup")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
