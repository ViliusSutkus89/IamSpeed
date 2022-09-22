/*
 * .kt
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

package com.viliussutkus89.iamspeed.rule

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.viliussutkus89.iamspeed.service.SpeedListenerService
import com.viliussutkus89.iamspeed.ui.IamSpeedActivity
import org.junit.rules.ExternalResource


class IdlingResourceRule(private val activityScenario: ActivityScenarioRule<IamSpeedActivity>): ExternalResource() {
    private val idlingResources = SpeedListenerService.idlingResources.toMutableList()

    override fun before() {
        activityScenario.scenario.onActivity {
            idlingResources.add(it.locationSettingsChangedIdlingResource!!)
            idlingResources.add(it.serviceCanBeStartedOnStartupIdlingResource!!)
        }
        IdlingRegistry.getInstance().let { idlingRegistry ->
            idlingResources.forEach { idlingResource ->
                idlingRegistry.register(idlingResource)
            }
        }
    }

    override fun after() {
        IdlingRegistry.getInstance().let { idlingRegistry ->
            idlingResources.forEach { idlingResource ->
                idlingRegistry.unregister(idlingResource)
            }
        }
    }
}
