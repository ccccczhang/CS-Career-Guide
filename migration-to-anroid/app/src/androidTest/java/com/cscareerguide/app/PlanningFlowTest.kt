package com.cscareerguide.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlanningFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun userCanCompleteTheMockPlanningFlow() {
        login()
        composeRule.onNodeWithTag("tab_plan").performClick()
        waitFor("screen_career_paths")

        composeRule.onNodeWithTag("career_path_card_employment").performClick()
        waitFor("screen_career_path_detail")
        composeRule.onNodeWithTag("start_introduction").performClick()
        waitFor("screen_self_introduction")

        listOf(
            "intro_name" to "示例同学",
            "intro_school" to "示例大学",
            "intro_major" to "计算机科学与技术",
            "intro_grade" to "大三",
            "intro_goal" to "Java 后端",
        ).forEach { (tag, value) ->
            composeRule.onNodeWithTag(tag).performScrollTo().performTextInput(value)
        }
        composeRule.onNodeWithTag("skill_technical_0").performScrollTo().performClick()
        composeRule.onNodeWithTag("intro_next").performClick()
        waitFor("screen_career_evaluation")

        composeRule.onNodeWithTag("career_list")
            .performScrollToNode(hasTestTag("career_option_Java"))
        composeRule.onNodeWithTag("career_option_Java").performClick()
        composeRule.onNodeWithTag("generate_plan").performClick()
        waitFor("screen_career_plan")
        assertTrue(
            composeRule.onAllNodesWithText("Java")
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
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
        waitFor("screen_home")
    }

    private fun waitFor(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
