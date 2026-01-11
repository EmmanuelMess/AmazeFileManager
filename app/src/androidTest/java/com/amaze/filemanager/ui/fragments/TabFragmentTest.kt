package com.amaze.filemanager.ui.fragments

import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.amaze.filemanager.R
import com.amaze.filemanager.test.StoragePermissionHelper
import com.amaze.filemanager.ui.activities.MainActivity
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [TabFragment] functionality, mainly for
 * https://github.com/TeamAmaze/AmazeFileManager/issues/1555.
 *
 * Note: deprecated methods and classes are used here for best reproducing the issues.
 */
@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class TabFragmentTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    val storagePermissionRule: GrantPermissionRule =
        GrantPermissionRule
            .grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Rule
    @JvmField
    val notificationPermissionRule: GrantPermissionRule =
        if (SDK_INT >= TIRAMISU) {
            GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @Before
    fun grantManageStoragePermission() {
        StoragePermissionHelper.grantManageStoragePermission()
    }

    /**
     * This test causes a rotation to happen while the MainFragment detaches, to check if it
     * fails. This could happen in reality, but should be very rare
     */
    @Test
    fun testFragmentStateSavingDuringDetachment() {
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Get the TabFragment
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activity = activityRule.activity
            val tabFragment =
                activity.supportFragmentManager
                    .findFragmentById(R.id.content_frame) as TabFragment

            // Detach fragment through FragmentManager
            activity.supportFragmentManager.beginTransaction().apply {
                tabFragment.fragments.forEach { detach(it) }
                commit()
            }
        }
    }

    /**
     * Check if the fragment state is saved correctly during a configuration change
     * by rotate the screen while swiping between the tabs.
     */
    @SdkSuppress(excludedSdks = [21, 28])
    @Test
    fun testFragmentStateSavingDuringConfigChange() {
        // First perform the swipe action
        swipeLeftCompat(withId(R.id.pager))

        // Force a configuration change by rotating the screen
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Thread.sleep(1000) // Give time for the rotation to complete
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * Check if the fragment state is saved correctly during rapid tab swiping.
     */
    @SdkSuppress(excludedSdks = [21, 28])
    @Test
    fun testRapidTabSwitchingAndStateSaving() {
        // Perform rapid tab switches
        repeat(10) {
            swipeLeftCompat(withId(R.id.pager))
            Thread.sleep(100) // Small delay to ensure swipe completes
            swipeRightCompat(withId(R.id.pager))
            Thread.sleep(100) // Small delay to ensure swipe completes
        }

        // Force a save state by rotating
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    /**
     * Check if the fragment state is saved correctly when the fragment is detached.
     */
    @SdkSuppress(excludedSdks = [21, 28])
    @Test
    fun testFragmentDetachmentAndStateSaving() {
        // First switch to a different tab
        swipeLeftCompat(withId(R.id.pager))

        // Get the TabFragment
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activity = activityRule.activity
            val tabFragment =
                activity.supportFragmentManager
                    .findFragmentById(R.id.content_frame) as TabFragment

            // Detach fragment through FragmentManager
            activity.supportFragmentManager.beginTransaction().apply {
                tabFragment.fragments.firstOrNull()?.let { detach(it) }
                commit()
            }
        }

        // Force state save through configuration change
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    /**
     * Hack that works like swipeLeft or swipeRight on smaller screens
     */
    private fun swipeHack(
        interpolatorX: Float,
        interpolatorY: Float,
        viewMatcher: Matcher<View>,
    ) {
        /* HACK
         If the View items are contained inside a ScrollView, and the screen's height is not
          enough to show 90% of the ScrollView, an error is thrown. This is a problem for smaller
          screens, to fix this we simply run the swipe "manually".
          See https://stackoverflow.com/a/74361805/3124150
         */

        onView(viewMatcher).perform(
            object : ViewAction {
                override fun getConstraints(): Matcher<View> = isDisplayed()

                override fun getDescription(): String = "Swipe without checking rect availability"

                override fun perform(
                    uiController: UiController,
                    view: View,
                ) {
                    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    val visibleRect = Rect()
                    view.getGlobalVisibleRect(visibleRect)

                    val endX = visibleRect.left + (visibleRect.right * interpolatorX).toInt()
                    val endY = visibleRect.top + (visibleRect.bottom * interpolatorY).toInt()

                    // Swipe up from the center, at 5ms per step
                    device.swipe(
                        visibleRect.centerX(),
                        visibleRect.centerY(),
                        endX,
                        endY,
                        10,
                    )
                }
            },
        )
    }

    private fun swipeLeftCompat(viewMatcher: Matcher<View>) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        if (device.displayHeight <= 1280) {
            swipeHack(0.95f, 0.5f, viewMatcher)
        } else {
            onView(viewMatcher).perform(swipeLeft())
        }
    }

    private fun swipeRightCompat(viewMatcher: Matcher<View>) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        if (device.displayHeight <= 1280) {
            swipeHack(0.05f, 0.5f, viewMatcher)
        } else {
            onView(viewMatcher).perform(swipeRight())
        }
    }
} 
