/*
 * Autostart.kt
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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.viliussutkus89.iamspeed.rule.CloseSystemDialogsTestRule
import com.viliussutkus89.iamspeed.rule.IdlingResourceRule
import com.viliussutkus89.iamspeed.rule.ScreenshotFailedTestRule
import com.viliussutkus89.iamspeed.ui.IamSpeedActivity
import com.viliussutkus89.iamspeed.utils.LocationControl
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class Autostart {
    companion object {
        @BeforeClass
        @JvmStatic
        fun enableGps() {
            LocationControl(InstrumentationRegistry.getInstrumentation()).enableGps()
        }

        @AfterClass
        @JvmStatic
        fun disableLocation() {
            LocationControl(InstrumentationRegistry.getInstrumentation()).disable()
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
    fun autostart() {
        onView(withId(R.id.speed))
            .check(matches(isDisplayed()))
    }
}
