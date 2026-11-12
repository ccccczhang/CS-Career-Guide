package com.cscareerguide.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CareerPrepFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun userCanBrowseJobsOpenResumeAndFinishAMockInterview() {
        login()

        composeRule.onNodeWithTag("tab_career").performClick()
        waitForTag("screen_company_reviews")
        composeRule.onNodeWithTag("hunting_tab_red").performClick()
        assertTrue(
            composeRule.onAllNodesWithText("红榜 · 值得关注")
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )

        composeRule.onNodeWithTag("company_resume").performClick()
        waitForTag("screen_resume")
        composeRule.onNodeWithTag("resume_pane_preview").performClick()
        waitForTag("resume_preview")
        composeRule.onNodeWithTag("route_back").performClick()
        waitForTag("screen_company_reviews")

        composeRule.onNodeWithTag("tab_learn").performClick()
        waitForTag("screen_interview")
        composeRule.onNodeWithTag("interview_start").performScrollTo().performClick()
        waitForTag("interview_timer")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("面试官")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithTag("interview_finish").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("面试复盘 · 技术面")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun login() {
        composeRule.onNodeWithTag("login_username").performTextInput("student")
        composeRule.onNodeWithTag("login_password").performTextInput("123456")
        composeRule.onNodeWithTag("login_submit").performScrollTo().performClick()
        waitForTag("screen_home")
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
