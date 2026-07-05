package com.spoludo.valens.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.spoludo.valens.ui.theme.ValensTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_showsTagline() {
        composeTestRule.setContent {
            ValensTheme {
                HomeScreen()
            }
        }
        composeTestRule.onNodeWithText("Train for life, not records.").assertExists()
    }
}
