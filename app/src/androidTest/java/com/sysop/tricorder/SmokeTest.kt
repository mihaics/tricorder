package com.sysop.tricorder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun mapScreenIsDisplayed() {
        // Verify the map screen appears as start destination
        // The MapScreen should show category tabs
        composeRule.onNodeWithText("Motion").assertIsDisplayed()
    }

    @Test
    fun categoryTabsAreVisible() {
        // Verify category filter chips are displayed
        composeRule.onNodeWithText("Motion").assertIsDisplayed()
        composeRule.onNodeWithText("Environment").assertIsDisplayed()
        composeRule.onNodeWithText("Location").assertIsDisplayed()
        composeRule.onNodeWithText("RF Scanner").assertIsDisplayed()
    }

    @Test
    fun categoryTabToggles() {
        // Tap a category tab and verify it toggles
        composeRule.onNodeWithText("Motion").performClick()
        // After toggling, the tab should still be visible
        composeRule.onNodeWithText("Motion").assertIsDisplayed()
    }

    @Test
    fun navigateToSettings() {
        // Navigate to settings screen
        composeRule.onNodeWithText("Settings").performClick()
        // Verify settings screen appears
        composeRule.onNodeWithText("API Keys").assertIsDisplayed()
    }
}
