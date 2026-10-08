package com.rejwane.reelslocal.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.rejwane.reelslocal.ui.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BottomNavBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersAllFourTabs() {
        composeRule.setContent {
            MaterialTheme {
                BottomNavBar(currentRoute = Routes.HOME, unreadCount = 0, onTabSelected = {})
            }
        }
        composeRule.onNodeWithContentDescription("Home").assertExists()
        composeRule.onNodeWithContentDescription("Explore").assertExists()
        composeRule.onNodeWithContentDescription("Inbox").assertExists()
        composeRule.onNodeWithContentDescription("Profile").assertExists()
    }

    @Test
    fun clickingTabReportsSelection() {
        var selected: BottomTab? = null
        composeRule.setContent {
            MaterialTheme {
                BottomNavBar(
                    currentRoute = Routes.HOME,
                    unreadCount = 0,
                    onTabSelected = { selected = it }
                )
            }
        }
        composeRule.onNodeWithContentDescription("Explore").performClick()
        assertEquals(BottomTab.EXPLORE, selected)
    }

    @Test
    fun showsUnreadBadgeOnInbox() {
        composeRule.setContent {
            MaterialTheme {
                BottomNavBar(currentRoute = Routes.HOME, unreadCount = 5, onTabSelected = {})
            }
        }
        composeRule.onNodeWithText("5").assertExists()
    }
}
