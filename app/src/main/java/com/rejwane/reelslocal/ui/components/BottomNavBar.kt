package com.rejwane.reelslocal.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.ui.navigation.Routes

enum class BottomTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(Routes.HOME, R.string.tab_home, Icons.Outlined.Home, Icons.Rounded.Home),
    EXPLORE(Routes.EXPLORE, R.string.tab_explore, Icons.Outlined.Explore, Icons.Rounded.Explore),
    INBOX(Routes.INBOX, R.string.tab_inbox, Icons.Outlined.Notifications, Icons.Rounded.Notifications),
    PROFILE(Routes.PROFILE, R.string.tab_profile, Icons.Outlined.Person, Icons.Rounded.Person)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    unreadCount: Int,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        BottomTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            val description = stringResource(tab.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == BottomTab.INBOX && unreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.icon,
                                contentDescription = description
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.icon,
                            contentDescription = description
                        )
                    }
                },
                label = { Text(description) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
