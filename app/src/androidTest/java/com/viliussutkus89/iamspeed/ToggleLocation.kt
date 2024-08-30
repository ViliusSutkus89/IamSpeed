/*
 * ToggleLocation.kt
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
import android.content.Intent
import androidx.annotation.RequiresApi
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.viliussutkus89.iamspeed.rule.CloseSystemDialogsTestRule
import com.viliussutkus89.iamspeed.rule.IdlingResourceRule
import com.viliussutkus89.iamspeed.rule.ScreenshotFailedTestRule
import com.viliussutkus89.iamspeed.service.SpeedListenerService
import com.viliussutkus89.iamspeed.ui.IamSpeedActivity
import com.viliussutkus89.iamspeed.utils.LocationControl
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.rules.RuleChain
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@Suppress
class ToggleLocation {
    companion object {
        private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

        @BeforeClass @AfterClass @JvmStatic
        fun disableLocation() {
            LocationControl(instrumentation).disable()
        }

        private fun enableGps() {
            LocationControl(instrumentation).enableGps()
        }

        @After
        fun stopService() {
            instrumentation.targetContext.sendBroadcast(Intent(SpeedListenerService.STOP_BROADCAST_ACTION))
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

    private fun toggleLocationSetting(openLocationManager: Boolean) {
        val uiDevice = if (openLocationManager) {
            androidx.test.uiautomator.UiDevice.getInstance(instrumentation)
        } else null

        for (i in 1..3) {
            onView(withId(R.id.speed))
                .check(matches(not(isDisplayed())))
            Assert.assertFalse(SpeedListenerService.started.value!!)
            onView(withId(R.id.button_enable_location))
                .check(matches(isDisplayed())).also {
                    if (openLocationManager) {
                        it.perform(click())
                    }
                }

            scenarioRule.scenario.onActivity {
                it.locationSettingsChangedIdlingResource!!.increment()
                enableGps()
            }

            // @TODO: might be possible to use uiAutomator to wait until
            // LocationManager activity is displayed on screen proper
            // Wouldn't need to use failure handler below
            uiDevice?.pressBack()

            onView(withId(R.id.speed)).withFailureHandler { error, _ ->
                    uiDevice?.let {
                        it.pressBack()
                        onView(withId(R.id.speed)).check(matches(isDisplayed()))
                    } ?: run { throw error }
                }
                .check(matches(isDisplayed()))

            Assert.assertTrue(SpeedListenerService.started.value!!)
            onView(withId(R.id.button_enable_location))
                .check(matches(not(isDisplayed())))

            scenarioRule.scenario.onActivity {
                it.locationSettingsChangedIdlingResource!!.increment()
                SpeedListenerService.locationSettingsChangedIdlingResource!!.increment()
                disableLocation()
            }
        }
    }

    @Test
    fun toggleWhileStayingInFragment() {
        toggleLocationSetting(openLocationManager = false)
    }

    // Back button needs to be pressed to navigate back to the app from
    // external location manager. UiAutomation is used for this, but
    // it requires API 18
    @RequiresApi(18) @SdkSuppress(minSdkVersion = 18)
    @Test
    fun toggleByOpeningExternalLocationManager() {
        toggleLocationSetting(openLocationManager = true)
    }

    private fun openSettingsFragment() {
        onView(anyOf(
            withId(R.id.settings),
            withText(R.string.menu_settings),
        )).withFailureHandler { _, viewMatcher ->
            Espresso.openActionBarOverflowOrOptionsMenu(instrumentation.targetContext)
            onView(viewMatcher).perform(click())
        }.perform(click())
    }

    private fun incrementIdlingResource_unregister_pressBack_registerItAgain() {
        // Increment resource, unregister it, press back, register it again.
        // Back button can't be pressed while idling resource is not idle.
        // The purpose of this idling resource is to detect
        // when things after back button press are completed.
        scenarioRule.scenario.onActivity {
            it.serviceCanBeStartedOnStartupIdlingResource!!.let { ir ->
                ir.increment()
                IdlingRegistry.getInstance().unregister(ir)
            }
        }

        Espresso.pressBack()

        scenarioRule.scenario.onActivity {
            IdlingRegistry.getInstance().register(it.serviceCanBeStartedOnStartupIdlingResource!!)
        }
    }

    private fun waitForIdlingResources() {
        // check for literally any view
        onView(withId(R.id.nav_host_fragment))
            .withFailureHandler { _, _ -> }
            .check(matches(isDisplayed()))
    }

    @Test
    fun disableWhileInSettingsFragment() {
        scenarioRule.scenario.onActivity {
            it.locationSettingsChangedIdlingResource!!.increment()
            enableGps()
        }

        onView(withId(R.id.speed))
            .check(matches(isDisplayed()))
        Assert.assertTrue(SpeedListenerService.started.value!!)
        onView(withId(R.id.button_enable_location))
            .check(matches(not(isDisplayed())))

        openSettingsFragment()

        SpeedListenerService.locationSettingsChangedIdlingResource!!.increment()
        disableLocation()
        waitForIdlingResources()

        Assert.assertFalse(SpeedListenerService.started.value!!)

        incrementIdlingResource_unregister_pressBack_registerItAgain()

        // Add single a single retry when returning from other fragment
        // Could be handled by idling resource
        onView(withId(R.id.speed)).withFailureHandler { _, _ ->
                onView(withId(R.id.speed)).check(matches(not(isDisplayed())))
            }
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.speed))
            .check(matches(not(isDisplayed())))
        Assert.assertFalse(SpeedListenerService.started.value!!)
        onView(withId(R.id.button_enable_location))
            .check(matches(isDisplayed()))
    }

    @Test
    fun enableWhileInSettingsFragment() {
        onView(withId(R.id.speed))
            .check(matches(not(isDisplayed())))
        Assert.assertFalse(SpeedListenerService.started.value!!)
        onView(withId(R.id.button_enable_location))
            .check(matches(isDisplayed()))

        openSettingsFragment()

        enableGps()

        // No idling resources are supposed to be here.
        // Wait should be a NOOP
        waitForIdlingResources()
        // Just sleep for a while to make sure that state isn't changed
        Thread.sleep(100L)
        Assert.assertFalse(SpeedListenerService.started.value!!)

        incrementIdlingResource_unregister_pressBack_registerItAgain()

        onView(withId(R.id.speed))
            .check(matches(isDisplayed()))
        Assert.assertTrue(SpeedListenerService.started.value!!)
        onView(withId(R.id.button_enable_location))
            .check(matches(not(isDisplayed())))
    }
}
