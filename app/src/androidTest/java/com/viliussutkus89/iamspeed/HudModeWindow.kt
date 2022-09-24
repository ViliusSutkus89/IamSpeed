/*
 * HudModeWindow.kt
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
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
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
import org.junit.*
import org.junit.rules.RuleChain
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class HudModeWindow {
    companion object {
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

    private fun checkIfActionBar(displayed: Boolean) {
        val actionBar = Matchers.anyOf(
            withId(R.id.hud),
            withText(R.string.menu_hud),

            withText(androidx.appcompat.R.string.abc_action_menu_overflow_description),
            withContentDescription(androidx.appcompat.R.string.abc_action_menu_overflow_description),

            withText(androidx.appcompat.R.string.abc_action_bar_up_description),
            withContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description),
        )

        if (displayed) {
            onView(Matchers.allOf(isDisplayed(), actionBar))
        } else {
            onView(actionBar).check(matches(Matchers.not(isDisplayed())))
        }
    }

    @Before
    fun enterHudMode() {
        checkIfActionBar(true)

        onView(
            Matchers.anyOf(
                withId(R.id.hud),
                withText(R.string.menu_hud),
            )
        ).withFailureHandler { _, viewMatcher ->
            Espresso.openActionBarOverflowOrOptionsMenu(instrumentation.targetContext)
            onView(viewMatcher).perform(ViewActions.click())
        }.perform(ViewActions.click())

        onView(withId(R.id.speed)).check(matches(isDisplayed()))
        Assert.assertTrue(SpeedListenerService.started.value!!)
    }

    @Test
    fun actionBarDisappears() {
        checkIfActionBar(false)
    }

    @Test
    fun actionBarRestored() {
        Espresso.pressBack()
        checkIfActionBar(true)
    }
}
