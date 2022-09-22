/*
 * CountingIdlingResourceFactory.kt
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

package com.viliussutkus89.iamspeed.util

import androidx.test.espresso.idling.CountingIdlingResource


internal class CountingIdlingResourceFactory {
    companion object {
        private val isRunningTest: Boolean = try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }

        internal fun create(resourceName: String): CountingIdlingResource? {
            return if (isRunningTest) {
                CountingIdlingResource(resourceName, true)
            } else {
                null
            }
        }
    }
}
