package com.cscareerguide.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun everyNonHomeTabCanReturnToHome() {
        login()

        listOf("plan", "career", "learn", "profile").forEach { tab ->
            composeRule.onNodeWithTag("tab_$tab").performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithTag("tab_home").performClick()
            composeRule.waitForIdle()
            composeRule.onAllNodesWithTag("screen_home")
                .fetchSemanticsNodes()
                .also {
                    if (it.isEmpty()) {
                        composeRule.onRoot().printToLog("NAV_TEST")
                    }
                }
                .let { nodes -> org.junit.Assert.assertTrue("Unable to return home from $tab", nodes.isNotEmpty()) }
        }
    }

    @Test
    fun profileTabAndShortcutRemainReachableAfterNestedPages() {
        login()

        composeRule.onNodeWithTag("tab_profile").performClick()
        waitForTag("screen_profile")
        composeRule.onNodeWithText("练习记录 · 3").performScrollTo().performClick()
        waitForTag("screen_platform_records")
        pressBack()
        waitForTag("screen_profile")

        composeRule.onNodeWithTag("tab_plan").performClick()
        composeRule.onNodeWithTag("tab_learn").performClick()
        composeRule.onNodeWithTag("tab_profile").performClick()
        waitForTag("screen_profile")

        composeRule.onNodeWithTag("tab_home").performClick()
        waitForTag("screen_home")
        composeRule.onNodeWithTag("home_profile").performClick()
        waitForTag("screen_profile")
    }

    @Test
    fun profileTabRemainsReachableAfterActivityRecreation() {
        login()

        composeRule.onNodeWithTag("tab_profile").performClick()
        waitForTag("screen_profile")
        composeRule.activityRule.scenario.recreate()
        waitForTag("screen_profile")

        composeRule.onNodeWithTag("tab_plan").performClick()
        composeRule.onNodeWithTag("tab_profile").performClick()
        waitForTag("screen_profile")
    }

    @Test
    fun profileTabRemainsReachableAfterSystemBack() {
        login()

        composeRule.onNodeWithTag("tab_profile").performClick()
        waitForTag("screen_profile")
        pressBack()
        waitForTag("screen_home")
        composeRule.onNodeWithTag("tab_profile").performClick()
        waitForTag("screen_profile")
        pressBack()
        waitForTag("screen_home")

        composeRule.onNodeWithTag("tab_plan").performClick()
        composeRule.onNodeWithTag("tab_profile").performClick()
        waitForTag("screen_profile")
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun login() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("screen_home").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("login_username").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("screen_home").fetchSemanticsNodes().isNotEmpty()) return
        composeRule.onNodeWithTag("login_username").performTextInput(ServerTestAccount.username)
        composeRule.onNodeWithTag("login_password").performTextInput(ServerTestAccount.password)
        composeRule.onNodeWithTag("login_submit").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("home_start_plan")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
