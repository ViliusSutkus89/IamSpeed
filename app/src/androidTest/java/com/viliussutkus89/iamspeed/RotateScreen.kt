/*
 * RotateScreen.kt
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
import android.content.pm.ActivityInfo
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.viliussutkus89.iamspeed.rule.CloseSystemDialogsTestRule
import com.viliussutkus89.iamspeed.rule.IdlingResourceRule
import com.viliussutkus89.iamspeed.rule.ScreenshotFailedTestRule
import com.viliussutkus89.iamspeed.service.SpeedListenerService
import com.viliussutkus89.iamspeed.ui.IamSpeedActivity
import com.viliussutkus89.iamspeed.utils.LocationControl
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class RotateScreen {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setMasterIdlingPolicyTimeout() {
            // Hoping this will give more time to start Activity in cloud emulator
            IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.MINUTES)
        }

        private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

        @BeforeClass
        @JvmStatic
        fun enableGps() {
            LocationControl(instrumentation).enableGps()
        }

        @AfterClass
        @JvmStatic
        fun disableLocation() {
            LocationControl(instrumentation).disable()
        }
    }

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    private val scenarioRule = activityScenarioRule<IamSpeedActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(scenarioRule)
        .around(CloseSystemDialogsTestRule())
        .around(ScreenshotFailedTestRule(scenarioRule))
        .around(IdlingResourceRule(scenarioRule))

    @Test
    fun rotateIamSpeedFragment() {
        onView(withId(R.id.speed)).check(matches(isDisplayed()))

        for (i in 0..32) {
            scenarioRule.scenario.onActivity { activity ->
                val idlingResource = (activity.application as IamSpeedApplication).configurationChangedIdlingResource!!
                idlingResource.increment()
                val idlingRegistry = IdlingRegistry.getInstance()
                idlingRegistry.unregister(idlingResource)
                activity.requestedOrientation = when (i % 4) {
                    0 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    1 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    2 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                }
                idlingRegistry.register(idlingResource)
            }

            onView(withId(R.id.speed)).check(matches(isDisplayed()))
            Assert.assertTrue(SpeedListenerService.started.value!!)
            onView(withId(R.id.button_enable_location)).check(matches(not(isDisplayed())))
        }
    }
}
