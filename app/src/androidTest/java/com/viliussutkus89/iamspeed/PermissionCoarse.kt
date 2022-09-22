/*
 * PermissionCoarse.kt
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

package com.viliussutkus89.iamspeed

import android.Manifest
import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.viliussutkus89.iamspeed.rule.CloseSystemDialogsTestRule
import com.viliussutkus89.iamspeed.rule.IdlingResourceRule
import com.viliussutkus89.iamspeed.rule.ScreenshotFailedTestRule
import com.viliussutkus89.iamspeed.ui.IamSpeedActivity
import com.viliussutkus89.iamspeed.utils.LocationControl
import org.hamcrest.Matchers.not
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.M)
class PermissionCoarse {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setMasterIdlingPolicyTimeout() {
            // Hoping this will give more time to start Activity in cloud emulator
            IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.MINUTES)
        }

        @BeforeClass
        @JvmStatic
        fun disableGps() {
            LocationControl(InstrumentationRegistry.getInstrumentation()).enableGps()
        }
    }

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION)

    private val scenarioRule = activityScenarioRule<IamSpeedActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(scenarioRule)
        .around(CloseSystemDialogsTestRule())
        .around(ScreenshotFailedTestRule(scenarioRule))
        .around(IdlingResourceRule(scenarioRule))

    @Test
    fun iHaveSomeRights() {
        onView(withId(R.id.request_location_permission))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.request_fine_location_permission))
            .check(matches(isDisplayed()))

        onView(withId(R.id.speed))
            .check(matches(isDisplayed()))
    }
}
