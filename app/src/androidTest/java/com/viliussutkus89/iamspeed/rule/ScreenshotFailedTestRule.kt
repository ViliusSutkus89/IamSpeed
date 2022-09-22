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

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.screenshot.BasicScreenCaptureProcessor
import androidx.test.runner.screenshot.Screenshot
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.IOException


class ScreenshotFailedTestRule<T>(private val scenario: ActivityScenarioRule<T>): TestWatcher() where T: Activity {

    companion object {
        private const val TAG = "ScreenFailedTestRule"
    }

    private val processor = object: BasicScreenCaptureProcessor() {
        init { mDefaultScreenshotPath = getStorageDir() }
        private fun getStorageDir(): File? {
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            listOfNotNull(
                File("/data/local/tmp"),
                ctx.cacheDir,
                ctx.externalCacheDir
            ).map { File(it, "TestScreenshots") }
            .forEach {
                it.mkdirs()
                if (it.isDirectory && it.canWrite()) {
                    Log.v(TAG, "setting mDefaultScreenshotPath to " + it.path)
                    return it
                }
            }
            return null
        }
    }

    // Activity required for API level <18
    private lateinit var activity: Activity

    override fun starting(description: Description?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            scenario.scenario.onActivity {
                activity = it
            }
        }
    }

    override fun failed(e: Throwable, description: Description) {
        val capture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Screenshot.capture()
        } else {
            Screenshot.capture(activity)
        }.apply {
            name = description.testClass.simpleName + "-" + description.methodName
            format = Bitmap.CompressFormat.PNG
        }
        try {
            processor.process(capture)
        } catch (err: IOException) {
            err.printStackTrace()
        }
    }
}
