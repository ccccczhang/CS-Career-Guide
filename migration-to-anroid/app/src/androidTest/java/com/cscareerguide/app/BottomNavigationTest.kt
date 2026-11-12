package com.cscareerguide.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
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

    private fun login() {
        composeRule.onNodeWithTag("login_username").performTextInput("student")
        composeRule.onNodeWithTag("login_password").performTextInput("123456")
        composeRule.onNodeWithTag("login_submit").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("home_start_plan")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
